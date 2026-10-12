package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.questions.domain.converter.QuestionTypeConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.ExamGradeRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.ExamAttemptResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.ExamGradingResponse;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.tests.repository.TestStateRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Staff-side of scheduled exams: list attempts, view one, set or override per-question scores. */
@Service
public class ExamGradingService {

    private final TestRepository testRepository;
    private final TestStateRepository testStateRepository;
    private final TestStateService testStateService;

    public ExamGradingService(TestRepository testRepository, TestStateRepository testStateRepository,
                              TestStateService testStateService) {
        this.testRepository = testRepository;
        this.testStateRepository = testStateRepository;
        this.testStateService = testStateService;
    }

    public List<ExamAttemptResponse> listAttempts(UUID testId, User user) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found: " + testId));
        if (!testStateService.canGrade(user, testId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!test.isScheduledExam()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a scheduled exam.");
        }
        return testStateRepository.findByTest_Id(testId).stream()
                .sorted(Comparator.comparing((TestState s) -> s.getUser() == null ? "" : s.getUser().getUsername(), String.CASE_INSENSITIVE_ORDER))
                .map(s -> ExamAttemptResponse.builder()
                        .stateId(s.getId())
                        .userId(s.getUser() == null ? null : s.getUser().getId())
                        .username(s.getUser() == null ? "?" : s.getUser().getUsername())
                        .executionState(s.getExecutionState().name())
                        .startedAt(s.getStartTime())
                        .submittedAt(s.getSubmittedAt())
                        .totalScore(total(s))
                        .maxScore(max(s))
                        .pendingGrading(ExamSupport.pendingManualCount(s))
                        .build())
                .toList();
    }

    public ExamGradingResponse getGrading(UUID stateId, User user) {
        TestState state = testStateService.findTestStateForOwnerOrGrader(stateId, user);
        Test test = state.getTest();
        if (!test.isScheduledExam()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a scheduled exam.");
        }
        boolean grader = testStateService.canGrade(user, test.getId());
        if (!grader && !ExamSupport.reviewAvailable(test)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The review is not available yet.");
        }
        return ExamGradingResponse.builder()
                .stateId(state.getId())
                .testId(test.getId())
                .testName(test.getName())
                .userId(state.getUser() == null ? null : state.getUser().getId())
                .username(state.getUser() == null ? "?" : state.getUser().getUsername())
                .executionState(state.getExecutionState().name())
                .submittedAt(state.getSubmittedAt())
                .totalScore(total(state))
                .maxScore(max(state))
                .passingPercentage(state.getPassingPercentage() != null ? state.getPassingPercentage() : test.getPassingPercentage())
                .canGrade(grader)
                .items(state.getQuestions().stream().map(q -> ExamGradingResponse.Item.builder()
                        .questionStateId(q.getId())
                        .questionId(q.getQuestion().getId())
                        .questionType(QuestionTypeConverter.toQuestionType(q.getQuestion().getType()))
                        .maxScore(ExamSupport.maxScore(q))
                        .score(q.getScore() == null ? 0.0 : q.getScore())
                        .answered(q.isAnswered())
                        .manual(ExamSupport.isManual(q))
                        .graded(ExamSupport.isGraded(q))
                        .build()).toList())
                .build();
    }

    @Transactional
    public void grade(UUID stateId, ExamGradeRequest request, User user) {
        TestState state = testStateService.findTestStateForOwnerOrGrader(stateId, user);
        if (!testStateService.canGrade(user, state.getTest().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!state.getTest().isScheduledExam()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a scheduled exam.");
        }
        if (request.getGrades() == null) return;
        Map<UUID, QuestionState> byId = state.getQuestions().stream()
                .collect(Collectors.toMap(QuestionState::getId, Function.identity()));
        for (ExamGradeRequest.Grade grade : request.getGrades()) {
            QuestionState question = byId.get(grade.getQuestionStateId());
            if (question == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not part of this attempt");
            }
            double max = ExamSupport.maxScore(question);
            if (Double.isNaN(grade.getScore()) || grade.getScore() < 0 || grade.getScore() > max) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must be between 0 and " + max);
            }
            question.setScore(grade.getScore());
            question.setWasCorrectAnswer(max > 0 && grade.getScore() >= max);
            question.setGradedByStaff(true);
        }
        testStateRepository.save(state);
    }

    private double total(TestState s) {
        return s.getQuestions().stream().mapToDouble(q -> q.getScore() == null ? 0.0 : q.getScore()).sum();
    }

    private double max(TestState s) {
        return s.getQuestions().stream().mapToDouble(ExamSupport::maxScore).sum();
    }

}
