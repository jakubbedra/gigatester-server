package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.tests.domain.dto.enums.NavigateActionDto;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.*;
import com.konfyrm.gigatester.tests.repository.TestStateRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class TestStateService {

    private final QuestionStateService questionStateService;
    private final TestStateFactory testStateFactory;
    private final TestStateRepository testStateRepository;
    private final SubjectGroupAccessService accessService;

    @Autowired
    public TestStateService(
            QuestionStateService questionStateService,
            TestStateFactory testStateFactory,
            TestStateRepository testStateRepository,
            SubjectGroupAccessService accessService
    ) {
        this.questionStateService = questionStateService;
        this.testStateFactory = testStateFactory;
        this.testStateRepository = testStateRepository;
        this.accessService = accessService;
    }

    public TestState createTestState(UUID testId, TestStateRequest testStateRequest, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        TestState testState = testStateFactory.createTestState(testId, testStateRequest);
        testState.setUser(user);
        testStateRepository.findFirstByTest_IdAndUser_Id(testId, user.getId())
                .ifPresent(testStateRepository::delete);
        return testStateRepository.save(testState);
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

    public void updateTestExecutionState(UUID testStateId, NavigateActionDto navigateActionDto) {
        Optional<TestState> testStateOptional = testStateRepository.findById(testStateId);
        if (testStateOptional.isEmpty()) {
            throw new IllegalArgumentException("Unknown test state with id: " + testStateId);
        }
        TestState testState = testStateOptional.get();
        TestExecutionState executionState = testState.getExecutionState();
        if (TestExecutionState.getNext(executionState) == TestExecutionState.NOT_STARTED) {
            testStateFactory.resetTestState(testState);
        }
        if (testState.getMode() == TestMode.LEARNING && executionState == TestExecutionState.IN_PROGRESS && isLastQuestionIndex(testState)) {
            if (notAllQuestionsAnsweredCorrectly(testState)) {
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
                testState.setCurrentQuestionIndex(0);
                testState.setExecutionState(TestExecutionState.FINISHED);
            }
        } else if (executionState == TestExecutionState.IN_PROGRESS && testState.getMode() == TestMode.LEARNING && navigateActionDto == NavigateActionDto.FINISH) {
            if (notAllQuestionsAnsweredCorrectly(testState)) {
                testState.getQuestions().stream()
                        .filter(q -> !q.isWasCorrectAnswer())
                        .forEach(q -> QuestionStateResetStrategy.getStrategy(q.getQuestion().getType()).reset(q));
                testState.getQuestions().removeIf(QuestionState::isAnswered);
                testState.setCurrentQuestionIndex(0);
            } else {
                testState.setCurrentQuestionIndex(0);
                testState.setExecutionState(TestExecutionState.FINISHED);
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
                if (testState.getExecutionState() == TestExecutionState.IN_PROGRESS) {
                    testState.setExecutionState(TestExecutionState.IN_REVIEW);
                } else {
                    testState.setExecutionState(TestExecutionState.FINISHED);
                }
            }
        }


        testStateRepository.save(testState);
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
