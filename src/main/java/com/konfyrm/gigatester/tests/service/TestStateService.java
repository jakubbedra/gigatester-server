package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.metrics.service.DailyStreakService;
import com.konfyrm.gigatester.metrics.service.MetricsService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.tests.domain.dto.enums.NavigateActionDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestDisplayTypeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.*;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.tests.repository.TestStateRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestStateService {

    private final QuestionStateService questionStateService;
    private final TestStateFactory testStateFactory;
    private final TestStateRepository testStateRepository;
    private final SubjectGroupAccessService accessService;
    private final DailyStreakService streakService;
    private final MetricsService metricsService;
    private final TestRepository testRepository;
    private final PermissionService permissionService;

    /** Slack so a submit that was already in flight when the deadline hit is not rejected. */
    private static final long LATE_ANSWER_GRACE_MS = 10_000;

    @Autowired
    public TestStateService(
            QuestionStateService questionStateService,
            TestStateFactory testStateFactory,
            TestStateRepository testStateRepository,
            SubjectGroupAccessService accessService,
            DailyStreakService streakService,
            MetricsService metricsService,
            TestRepository testRepository,
            PermissionService permissionService
    ) {
        this.testRepository = testRepository;
        this.permissionService = permissionService;
        this.questionStateService = questionStateService;
        this.testStateFactory = testStateFactory;
        this.testStateRepository = testStateRepository;
        this.accessService = accessService;
        this.streakService = streakService;
        this.metricsService = metricsService;
    }

    public TestState createTestState(UUID testId, TestStateRequest testStateRequest, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found: " + testId));
        if (test.isScheduledExam()) {
            return createScheduledExamState(test, user);
        }
        TestState testState = testStateFactory.createTestState(testId, testStateRequest, user);
        testState.setUser(user);
        metricsService.applyWeightedShuffle(testState, user);
        testStateRepository.findFirstByTest_IdAndUser_Id(testId, user.getId())
                .ifPresent(testStateRepository::delete);
        return testStateRepository.save(testState);
    }

    /**
     * A scheduled exam is configured entirely by its Test (counts, passing %, time limit); the
     * student only starts it, and only once, only inside the window.
     */
    private TestState createScheduledExamState(Test test, User user) {
        Instant now = Instant.now();
        if (test.getStartAt() == null || test.getWindowEnd() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This exam has no schedule.");
        }
        if (now.isBefore(test.getStartAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The exam has not started yet.");
        }
        if (!now.isBefore(test.getWindowEnd())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The exam window has closed.");
        }
        if (testStateRepository.findFirstByTest_IdAndUser_Id(test.getId(), user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already taken this exam.");
        }
        long remainingMs = Duration.between(now, test.getWindowEnd()).toMillis();
        long limitMs = Math.min(test.getTimeLimit(), remainingMs);
        TestStateRequest request = TestStateRequest.builder()
                .closedQuestionsCount(test.getClosedQuestionsCount())
                .openQuestionsCount(test.getOpenQuestionsCount())
                .statementQuestionsCount(Integer.MAX_VALUE)
                .passingPercentage(test.getPassingPercentage())
                .mode(TestModeDto.EXAM)
                .displayType(TestDisplayTypeDto.ONE_BY_ONE)
                .timeLimitEnabled(true)
                .timeLimitMs(limitMs)
                .build();
        TestState state = testStateFactory.createTestState(test.getId(), request, user);
        state.setUser(user);
        return testStateRepository.save(state);
    }

    /** The attempt owner, or staff allowed to grade this test. */
    public TestState findTestStateForOwnerOrGrader(UUID stateId, User user) {
        TestState state = testStateRepository.findById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test state not found: " + stateId));
        boolean owner = state.getUser() != null && state.getUser().getId().equals(user.getId());
        if (!owner && !canGrade(user, state.getTest().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return state;
    }

    public boolean canGrade(User user, UUID testId) {
        return permissionService.hasTestPermission(user, testId, Permission.TESTS_WRITE);
    }

    /**
     * Guard for a student saving an answer: they must own the attempt, the question must belong to
     * it, and for a scheduled exam the attempt must still be running (staff grade afterwards).
     */
    public void assertCanAnswer(UUID testStateId, UUID questionStateId, User user) {
        TestState state = findTestState(testStateId, user.getId());
        if (state.getQuestions().stream().noneMatch(q -> q.getId().equals(questionStateId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not part of this attempt");
        }
        if (state.getTest().isScheduledExam()) {
            boolean overtime = state.isTimeLimitEnabled() && state.getTimeLimitMs() > 0
                    && System.currentTimeMillis() - state.getStartTime() > state.getTimeLimitMs() + LATE_ANSWER_GRACE_MS;
            if (state.getExecutionState() != TestExecutionState.IN_PROGRESS || overtime) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The exam is over.");
            }
        }
    }

    /** Reading answers: the owner, or staff who can grade, never another student. */
    public void assertCanReadAnswers(UUID testStateId, UUID questionStateId, User user) {
        TestState state = findTestStateForOwnerOrGrader(testStateId, user);
        if (state.getQuestions().stream().noneMatch(q -> q.getId().equals(questionStateId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not part of this attempt");
        }
    }

    public TestState findTestState(UUID stateId, UUID userId) {
        TestState state = testStateRepository.findById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test state not found: " + stateId));
        if (state.getUser() == null || !state.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return state;
    }

    public Optional<TestState> findTestStateByTestAndUser(UUID testId, UUID userId) {
        return testStateRepository.findFirstByTest_IdAndUser_Id(testId, userId);
    }

    @Transactional
    public void updateTestExecutionState(UUID testStateId, NavigateActionDto navigateActionDto) {
        Optional<TestState> testStateOptional = testStateRepository.findById(testStateId);
        if (testStateOptional.isEmpty()) {
            throw new IllegalArgumentException("Unknown test state with id: " + testStateId);
        }
        TestState testState = testStateOptional.get();
        TestExecutionState executionState = testState.getExecutionState();
        boolean scheduled = testState.getTest().isScheduledExam();
        if (scheduled && executionState != TestExecutionState.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A scheduled exam can only be taken once.");
        }
        if (TestExecutionState.getNext(executionState) == TestExecutionState.NOT_STARTED) {
            testStateFactory.resetTestState(testState);
        }
        if (testState.isTimeLimitEnabled()
                && testState.getTimeLimitMs() > 0
                && executionState == TestExecutionState.IN_PROGRESS
                && System.currentTimeMillis() - testState.getStartTime() > testState.getTimeLimitMs()) {
            testState.getQuestions().stream()
                    .filter(q -> isUnansweredQuestion(q, TestExecutionState.IN_PROGRESS))
                    .forEach(q -> questionStateService.checkQuestion(q, true));
            testState.getQuestions().stream()
                    .filter(q -> isUnansweredQuestion(q, TestExecutionState.IN_PROGRESS))
                    .forEach(q -> {
                        q.setAnswered(true);
                        q.setWasCorrectAnswer(false);
                        q.setScore(0.0);
                    });
            testState.setCurrentQuestionIndex(0);
            if (scheduled) {
                discardSelfAssignedManualScores(testState);
                testState.setExecutionState(TestExecutionState.FINISHED);
                testState.setSubmittedAt(System.currentTimeMillis());
            } else {
                testState.setExecutionState(TestExecutionState.IN_REVIEW);
            }
            testStateRepository.save(testState);
            return;
        }
        if (testState.getMode() == TestMode.LEARNING && executionState == TestExecutionState.IN_PROGRESS && isLastQuestionIndex(testState)) {
            if (notAllQuestionsAnsweredCorrectly(testState)) {
                accumulateRound(testState);
                testState.getQuestions().stream()
                        .filter(q -> !q.isWasCorrectAnswer())
                        .forEach(q -> QuestionStateResetStrategy.getStrategy(q.getQuestion().getType()).reset(q));
                testState.getQuestions().removeIf(QuestionState::isAnswered);
                testState.setCurrentQuestionIndex(0);
            } else {
                testState.getQuestions().stream()
                        .filter(q -> !q.isAnswered())
                        .forEach(q -> {
                            q.setAnswered(true);
                            q.setWasCorrectAnswer(false);
                            q.setScore(0.0);
                        });
                accumulateRound(testState);
                testState.setCurrentQuestionIndex(0);
                testState.setExecutionState(TestExecutionState.FINISHED);
                if (testState.getUser() != null) {
                    streakService.recordActivity(testState.getUser());
                    metricsService.recordTestCompletion(testState.getUser(), testState, testState.getCumulativeAttempted(), testState.getCumulativeCorrect());
                }
            }
        } else if (executionState == TestExecutionState.IN_PROGRESS && testState.getMode() == TestMode.LEARNING && navigateActionDto == NavigateActionDto.FINISH) {
            if (notAllQuestionsAnsweredCorrectly(testState)) {
                accumulateRound(testState);
                testState.getQuestions().stream()
                        .filter(q -> !q.isWasCorrectAnswer())
                        .forEach(q -> QuestionStateResetStrategy.getStrategy(q.getQuestion().getType()).reset(q));
                testState.getQuestions().removeIf(QuestionState::isAnswered);
                testState.setCurrentQuestionIndex(0);
            } else {
                accumulateRound(testState);
                testState.setCurrentQuestionIndex(0);
                testState.setExecutionState(TestExecutionState.FINISHED);
                if (testState.getUser() != null) {
                    streakService.recordActivity(testState.getUser());
                    metricsService.recordTestCompletion(testState.getUser(), testState, testState.getCumulativeAttempted(), testState.getCumulativeCorrect());
                }
            }
        } else if (executionState == TestExecutionState.IN_PROGRESS && testState.getMode() == TestMode.LEARNING) {
            testState.setCurrentQuestionIndex(testState.getCurrentQuestionIndex() + 1);
        } else if ((executionState == TestExecutionState.IN_PROGRESS || executionState == TestExecutionState.IN_REVIEW) && testState.getMode() == TestMode.EXAM) {
            if (navigateActionDto == NavigateActionDto.NEXT) {
                testState.setCurrentQuestionIndex(testState.getCurrentQuestionIndex() + 1);
            } else if (navigateActionDto == NavigateActionDto.PREVIOUS) {
                testState.setCurrentQuestionIndex(testState.getCurrentQuestionIndex() - 1);
            } else if (navigateActionDto == NavigateActionDto.FINISH) {
                testState.getQuestions().stream()
                        .filter(q -> isUnansweredQuestion(q, testState.getExecutionState()))
                        .forEach(q -> questionStateService.checkQuestion(q, true));
                testState.getQuestions().stream()
                        .filter(q -> isUnansweredQuestion(q, testState.getExecutionState()))
                        .forEach(q -> {
                            q.setAnswered(true);
                            q.setWasCorrectAnswer(false);
                            q.setScore(0.0);
                        });

                testState.setCurrentQuestionIndex(0);
                if (scheduled) {
                    discardSelfAssignedManualScores(testState);
                    testState.setExecutionState(TestExecutionState.FINISHED);
                    testState.setSubmittedAt(System.currentTimeMillis());
                    if (testState.getUser() != null) {
                        streakService.recordActivity(testState.getUser());
                    }
                } else if (testState.getExecutionState() == TestExecutionState.IN_PROGRESS) {
                    testState.setExecutionState(TestExecutionState.IN_REVIEW);
                } else {
                    testState.setExecutionState(TestExecutionState.FINISHED);
                    if (testState.getUser() != null) {
                        streakService.recordActivity(testState.getUser());
                        metricsService.recordTestCompletion(testState.getUser(), testState);
                    }
                }
            }
        }


        testStateRepository.save(testState);
    }

    /**
     * The answer endpoint accepts a client-supplied score for manual open questions (that is how
     * students self-grade in a standard exam). In a scheduled exam only staff may score them, so
     * anything not yet graded by staff starts at zero.
     */
    private void discardSelfAssignedManualScores(TestState testState) {
        testState.getQuestions().stream()
                .filter(q -> ExamSupport.isManual(q) && !ExamSupport.isGraded(q))
                .forEach(q -> {
                    q.setScore(0.0);
                    q.setWasCorrectAnswer(false);
                });
    }

    /**
     * Folds the current LEARNING round's outcome into the test's running total/correct
     * counters — must be called exactly once per round, right before that round's
     * QuestionStates get reset/orphan-removed for retry (or, for the final round, right
     * before completion is recorded). Per-question UserQuestionStat recording happens
     * separately, immediately on each submission (see recordLearningQuestionAttempt) —
     * not here — so an abandoned round's answers still count instead of being lost.
     */
    private void accumulateRound(TestState testState) {
        List<QuestionState> roundQuestions = testState.getQuestions();
        int roundTotal = roundQuestions.size();
        int roundCorrect = (int) roundQuestions.stream().filter(QuestionState::isWasCorrectAnswer).count();
        testState.setCumulativeAttempted(safeInt(testState.getCumulativeAttempted()) + roundTotal);
        testState.setCumulativeCorrect(safeInt(testState.getCumulativeCorrect()) + roundCorrect);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * Records a single LEARNING-mode question's attempt into UserQuestionStat the moment
     * it's submitted, rather than waiting for its round (or the whole test) to finish —
     * so per-question accuracy stats (tag accuracy, weighted shuffle) never lose an answer
     * to an abandoned test. Only the granularity of *when* this is recorded changes here;
     * the "Moje postępy" summary tiles still come from UserTestStat, written once at
     * completion as before (via recordTestCompletion, unaffected by this).
     */
    @Transactional
    public void recordLearningQuestionAttempt(UUID testStateId, QuestionState questionState) {
        if (!questionState.isAnswered()) return;
        testStateRepository.findById(testStateId).ifPresent(testState -> {
            if (testState.getMode() == TestMode.LEARNING && testState.getUser() != null) {
                metricsService.recordQuestionAttempts(testState.getUser(), List.of(questionState));
            }
        });
    }

    private boolean isUnansweredQuestion(QuestionState questionState, TestExecutionState testExecutionState) {
        if (questionState instanceof OpenQuestionState openQuestionState) {
            if (openQuestionState.getQuestion() instanceof OpenQuestion openQuestion) {
                if (isNotYetGraded(testExecutionState, openQuestion)) {
                    return false;
                }
            } else {
                throw new IllegalStateException("Open question state does not contain an open question.");
            }
        }
        return !questionState.isAnswered();
    }

    private boolean isNotYetGraded(TestExecutionState testExecutionState, OpenQuestion openQuestion) {
        return openQuestion.getGradingRulesHash().contains(GradingRule.MANUAL.getHash() + "")
                && testExecutionState == TestExecutionState.IN_PROGRESS;
    }

    private boolean isLastQuestionIndex(TestState testState) {
        return testState.getCurrentQuestionIndex() == testState.getQuestions().size() - 1;
    }

    private boolean notAllQuestionsAnsweredCorrectly(TestState testState) {
        return testState.getQuestions().stream()
                .anyMatch(q -> !q.isWasCorrectAnswer());
    }

}
