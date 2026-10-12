package com.konfyrm.gigatester.coding.service;

import com.konfyrm.gigatester.coding.domain.dto.request.CodingTaskRequest;
import com.konfyrm.gigatester.coding.domain.dto.response.CodingTaskResponse;
import com.konfyrm.gigatester.coding.domain.dto.response.CodingTaskSummaryResponse;
import com.konfyrm.gigatester.coding.domain.entity.CodingProblem;
import com.konfyrm.gigatester.coding.domain.entity.CodingSubmission;
import com.konfyrm.gigatester.coding.domain.entity.CodingTask;
import com.konfyrm.gigatester.coding.domain.entity.CodingTestCase;
import com.konfyrm.gigatester.coding.repository.CodingSubmissionRepository;
import com.konfyrm.gigatester.coding.repository.CodingTaskRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CodingTaskService {

    private final CodingTaskRepository taskRepository;
    private final CodingSubmissionRepository submissionRepository;
    private final SubjectRepository subjectRepository;

    public CodingTaskService(CodingTaskRepository taskRepository,
                             CodingSubmissionRepository submissionRepository,
                             SubjectRepository subjectRepository) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public CodingTask find(UUID id) {
        CodingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coding task not found: " + id));
        migrateLegacy(task);
        return task;
    }

    public List<CodingTask> findAll() {
        return taskRepository.findAll();
    }

    /**
     * Tasks created before a test could hold several problems keep their statement, starter code,
     * points and tests on the task itself. The first time such a task is read it becomes a test with
     * one problem built from those fields, and its old submissions are attached to that problem.
     */
    private void migrateLegacy(CodingTask task) {
        if (!task.getProblems().isEmpty() || task.getTests() == null || task.getTests().isEmpty()) return;
        CodingProblem problem = CodingProblem.builder()
                .title(task.getName())
                .description(task.getDescription())
                .starterCode(task.getStarterCode())
                .points(task.getPoints())
                .timeLimitMs(task.getTimeLimitMs())
                .tests(new ArrayList<>(task.getTests()))
                .build();
        task.getTests().clear();
        task.setDescription(null);
        task.getProblems().add(problem);
        taskRepository.saveAndFlush(task);
        List<CodingSubmission> old = submissionRepository.findByTask_IdAndProblemIsNull(task.getId());
        old.forEach(s -> s.setProblem(problem));
        submissionRepository.saveAll(old);
    }

    @Transactional
    public CodingTask add(CodingTaskRequest request, User creator) {
        CodingTask task = CodingTask.builder().createdBy(creator).build();
        apply(task, request);
        return taskRepository.save(task);
    }

    @Transactional
    public CodingTask update(UUID id, CodingTaskRequest request) {
        CodingTask task = find(id);
        apply(task, request);
        return taskRepository.save(task);
    }

    @Transactional
    public void delete(UUID id) {
        subjectRepository.findByCodingTasks_Id(id).forEach(s -> s.getCodingTasks().removeIf(t -> t.getId().equals(id)));
        submissionRepository.deleteByTask_Id(id);
        taskRepository.deleteById(id);
    }

    private void apply(CodingTask task, CodingTaskRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A programming test needs a name.");
        }
        boolean hasStart = request.getStartAt() != null;
        boolean hasEnd = request.getWindowEnd() != null;
        if (hasStart != hasEnd || (hasStart && !request.getWindowEnd().isAfter(request.getStartAt()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set both a start and a later window end, or neither.");
        }
        task.setName(request.getName().trim());
        task.setDescription(request.getDescription());
        task.setStartAt(request.getStartAt());
        task.setWindowEnd(request.getWindowEnd());

        Map<UUID, CodingProblem> existing = task.getProblems().stream()
                .collect(Collectors.toMap(CodingProblem::getId, Function.identity()));
        List<CodingProblem> result = new ArrayList<>();
        List<CodingTaskRequest.ProblemDto> dtos = request.getProblems() == null ? List.of() : request.getProblems();
        int n = 0;
        for (CodingTaskRequest.ProblemDto dto : dtos) {
            n++;
            if (dto.getPoints() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Points of problem " + n + " must be greater than zero.");
            }
            CodingProblem problem = dto.getId() != null && existing.containsKey(dto.getId())
                    ? existing.remove(dto.getId())
                    : CodingProblem.builder().build();
            problem.setTitle(dto.getTitle() == null || dto.getTitle().isBlank() ? "Problem " + n : dto.getTitle().trim());
            problem.setDescription(dto.getDescription());
            problem.setStarterCode(dto.getStarterCode());
            problem.setPoints(dto.getPoints());
            problem.setTimeLimitMs(dto.getTimeLimitMs() > 0 ? dto.getTimeLimitMs() : 5000);
            List<CodingTestCase> tests = new ArrayList<>();
            if (dto.getTests() != null) {
                for (CodingTaskRequest.TestCaseDto t : dto.getTests()) {
                    tests.add(CodingTestCase.builder()
                            .input(t.getInput() == null ? "" : t.getInput())
                            .expectedOutput(t.getExpectedOutput() == null ? "" : t.getExpectedOutput())
                            .hidden(t.isHidden())
                            .build());
                }
            }
            if (problem.getTests() == null) problem.setTests(new ArrayList<>());
            problem.getTests().clear();
            problem.getTests().addAll(tests);
            result.add(problem);
        }
        // Problems dropped from the list take their submissions with them.
        existing.values().forEach(p -> submissionRepository.deleteByProblem_Id(p.getId()));
        task.getProblems().clear();
        task.getProblems().addAll(result);
    }

    public CodingTaskResponse toResponse(CodingTask task) {
        return CodingTaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .startAt(task.getStartAt())
                .windowEnd(task.getWindowEnd())
                .problems(task.getProblems().stream().map(p -> CodingTaskResponse.Problem.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .starterCode(p.getStarterCode())
                        .points(p.getPoints())
                        .timeLimitMs(p.getTimeLimitMs())
                        .tests(p.getTests().stream().map(t -> CodingTaskResponse.TestCase.builder()
                                .input(t.getInput()).expectedOutput(t.getExpectedOutput()).hidden(t.isHidden()).build()).toList())
                        .build()).toList())
                .build();
    }

    public CodingTaskSummaryResponse toSummary(CodingTask task) {
        return CodingTaskSummaryResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .startAt(task.getStartAt())
                .windowEnd(task.getWindowEnd())
                .problemCount(task.getProblems() == null ? 0 : task.getProblems().size())
                .build();
    }

}
