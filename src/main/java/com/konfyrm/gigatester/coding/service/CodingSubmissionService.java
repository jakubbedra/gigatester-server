package com.konfyrm.gigatester.coding.service;

import com.konfyrm.gigatester.coding.domain.dto.request.SubmitCodingRequest;
import com.konfyrm.gigatester.coding.domain.dto.response.*;
import com.konfyrm.gigatester.coding.domain.entity.CodingProblem;
import com.konfyrm.gigatester.coding.domain.entity.CodingSubmission;
import com.konfyrm.gigatester.coding.domain.entity.CodingTask;
import com.konfyrm.gigatester.coding.domain.entity.CodingTestCase;
import com.konfyrm.gigatester.coding.repository.CodingSubmissionRepository;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** Student-facing play/submit logic and the staff results view. */
@Service
public class CodingSubmissionService {

    private static final Comparator<CodingSubmission> BEST_FIRST =
            Comparator.comparingDouble(CodingSubmission::getScore)
                    .thenComparing(CodingSubmission::getSubmittedAt, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final CodingTaskService taskService;
    private final CodingSubmissionRepository submissionRepository;
    private final SubjectGroupAccessService accessService;
    private final PermissionService permissionService;

    public CodingSubmissionService(CodingTaskService taskService,
                                   CodingSubmissionRepository submissionRepository,
                                   SubjectGroupAccessService accessService,
                                   PermissionService permissionService) {
        this.taskService = taskService;
        this.submissionRepository = submissionRepository;
        this.accessService = accessService;
        this.permissionService = permissionService;
    }

    public boolean canGrade(User user, UUID taskId) {
        return permissionService.hasCodingTaskPermission(user, taskId, Permission.CODING_WRITE);
    }

    private String phase(CodingTask task) {
        if (!task.isScheduled()) return "open";
        Instant now = Instant.now();
        if (now.isBefore(task.getStartAt())) return "notStarted";
        if (!now.isBefore(task.getWindowEnd())) return "closed";
        return "open";
    }

    private double maxScore(CodingTask task) {
        return task.getProblems().stream().mapToDouble(CodingProblem::getPoints).sum();
    }

    private CodingSubmission best(Collection<CodingSubmission> submissions) {
        return submissions.stream().max(BEST_FIRST).orElse(null);
    }

    /** A student is done once every problem has at least one submission from them. */
    private boolean isCompleted(CodingTask task, Collection<CodingSubmission> mine) {
        if (task.getProblems().isEmpty()) return false;
        Set<UUID> attempted = mine.stream().filter(s -> s.getProblem() != null)
                .map(s -> s.getProblem().getId()).collect(Collectors.toSet());
        return task.getProblems().stream().allMatch(p -> attempted.contains(p.getId()));
    }

    public CodingTaskPlayResponse play(UUID taskId, User user) {
        CodingTask task = taskService.find(taskId);
        if (!accessService.hasAccessToCodingTask(taskId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        boolean staff = canGrade(user, taskId);
        String phase = phase(task);
        // Nobody, staff included, sees the problems before the test opens; staff use the editor to look at them.
        boolean showContent = !phase.equals("notStarted");

        List<CodingSubmission> allMine = submissionRepository.findByTask_IdAndUser_Id(taskId, user.getId());
        Map<UUID, List<CodingSubmission>> mine = allMine.stream()
                .filter(s -> s.getProblem() != null)
                .collect(Collectors.groupingBy(s -> s.getProblem().getId()));

        double total = 0;
        List<CodingTaskPlayResponse.Problem> problems = new ArrayList<>();
        for (CodingProblem p : task.getProblems()) {
            List<CodingSubmission> list = mine.getOrDefault(p.getId(), List.of());
            CodingSubmission best = best(list);
            if (best != null) total += best.getScore();
            if (!showContent) continue;
            problems.add(CodingTaskPlayResponse.Problem.builder()
                    .id(p.getId())
                    .title(p.getTitle())
                    .description(p.getDescription())
                    .starterCode(p.getStarterCode())
                    .points(p.getPoints())
                    .timeLimitMs(p.getTimeLimitMs())
                    .tests(p.getTests().stream().map(t -> CodingTaskPlayResponse.TestCase.builder()
                            .input(t.getInput())
                            .expectedOutput(t.isHidden() && !staff ? null : t.getExpectedOutput())
                            .hidden(t.isHidden())
                            .build()).toList())
                    .bestScore(best == null ? null : best.getScore())
                    .bestPassed(best == null ? null : best.getPassedCount())
                    .attempts(list.size())
                    .build());
        }

        return CodingTaskPlayResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(showContent ? task.getDescription() : null)
                .startAt(task.getStartAt())
                .windowEnd(task.getWindowEnd())
                .phase(phase)
                .maxScore(maxScore(task))
                .totalScore(Math.round(total * 100.0) / 100.0)
                .completed(isCompleted(task, allMine))
                .problems(problems)
                .build();
    }

    public CodingSubmissionResponse submit(UUID taskId, UUID problemId, SubmitCodingRequest request, User user) {
        CodingTask task = taskService.find(taskId);
        if (!accessService.hasAccessToCodingTask(taskId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!canGrade(user, taskId) && !phase(task).equals("open")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This task is not open for submissions.");
        }
        List<CodingSubmission> before = submissionRepository.findByTask_IdAndUser_Id(taskId, user.getId());
        if (!canGrade(user, taskId) && isCompleted(task, before)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already finished this test.");
        }
        CodingProblem problem = task.getProblems().stream()
                .filter(p -> p.getId().equals(problemId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
        List<CodingTestCase> tests = problem.getTests();
        if (tests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This problem has no tests.");
        }
        List<String> outputs = request.getOutputs() == null ? List.of() : request.getOutputs();
        List<Boolean> results = new ArrayList<>();
        for (int i = 0; i < tests.size(); i++) {
            String actual = i < outputs.size() ? outputs.get(i) : null;
            results.add(actual != null && normalize(actual).equals(normalize(tests.get(i).getExpectedOutput())));
        }
        int passed = (int) results.stream().filter(Boolean::booleanValue).count();
        double score = Math.round(problem.getPoints() * passed / tests.size() * 100.0) / 100.0;

        CodingSubmission saved = submissionRepository.save(CodingSubmission.builder()
                .task(task)
                .problem(problem)
                .user(user)
                .code(request.getCode())
                .passedCount(passed)
                .totalCount(tests.size())
                .score(score)
                .submittedAt(Instant.now())
                .build());

        return CodingSubmissionResponse.builder()
                .submissionId(saved.getId())
                .passed(passed)
                .total(tests.size())
                .score(score)
                .maxScore(problem.getPoints())
                .results(results)
                .completed(isCompleted(task, concat(before, saved)))
                .build();
    }

    private List<CodingSubmission> concat(List<CodingSubmission> list, CodingSubmission extra) {
        List<CodingSubmission> all = new ArrayList<>(list);
        all.add(extra);
        return all;
    }

    /** Per student: their best attempt on each problem, summed, for the people who may grade this test. */
    public List<CodingResultRow> results(UUID taskId, User user) {
        if (!canGrade(user, taskId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        CodingTask task = taskService.find(taskId);
        Map<UUID, List<CodingSubmission>> byUser = submissionRepository.findByTask_Id(taskId).stream()
                .filter(s -> s.getProblem() != null)
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));
        double max = maxScore(task);

        return byUser.values().stream().map(list -> {
            User student = list.get(0).getUser();
            Map<UUID, List<CodingSubmission>> byProblem = list.stream()
                    .collect(Collectors.groupingBy(s -> s.getProblem().getId()));
            List<CodingResultRow.ProblemResult> perProblem = new ArrayList<>();
            double total = 0;
            for (CodingProblem p : task.getProblems()) {
                List<CodingSubmission> subs = byProblem.getOrDefault(p.getId(), List.of());
                CodingSubmission b = best(subs);
                if (b != null) total += b.getScore();
                perProblem.add(CodingResultRow.ProblemResult.builder()
                        .problemId(p.getId())
                        .title(p.getTitle())
                        .bestSubmissionId(b == null ? null : b.getId())
                        .bestScore(b == null ? 0 : b.getScore())
                        .maxScore(p.getPoints())
                        .bestPassed(b == null ? 0 : b.getPassedCount())
                        .total(b == null ? p.getTests().size() : b.getTotalCount())
                        .attempts(subs.size())
                        .build());
            }
            Instant last = list.stream().map(CodingSubmission::getSubmittedAt).filter(Objects::nonNull)
                    .max(Comparator.naturalOrder()).orElse(null);
            return CodingResultRow.builder()
                    .userId(student.getId())
                    .username(student.getUsername())
                    .totalScore(Math.round(total * 100.0) / 100.0)
                    .maxScore(max)
                    .attempts(list.size())
                    .lastSubmittedAt(last)
                    .problems(perProblem)
                    .build();
        }).sorted(Comparator.comparing(CodingResultRow::getUsername, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public CodingSubmissionDetailResponse detail(UUID submissionId, User user) {
        CodingSubmission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        if (!canGrade(user, s.getTask().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return CodingSubmissionDetailResponse.builder()
                .id(s.getId())
                .taskId(s.getTask().getId())
                .problemTitle(s.getProblem() == null ? null : s.getProblem().getTitle())
                .username(s.getUser().getUsername())
                .code(s.getCode())
                .passed(s.getPassedCount())
                .total(s.getTotalCount())
                .score(s.getScore())
                .maxScore(s.getProblem() == null ? 0 : s.getProblem().getPoints())
                .submittedAt(s.getSubmittedAt())
                .build();
    }

    /** Line endings, trailing spaces and trailing blank lines don't count as a difference. */
    static String normalize(String s) {
        if (s == null) return "";
        String[] lines = s.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line.replaceAll("\\s+$", "")).append('\n');
        }
        return sb.toString().replaceAll("\\n+$", "");
    }

}
