package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.TestExecutionState;
import com.konfyrm.gigatester.tests.domain.entity.TestMode;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.tests.repository.TestStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TestStateService {

    private final TestStateFactory testStateFactory;
    private final TestStateRepository testStateRepository;

    @Autowired
    public TestStateService(
            TestStateFactory testStateFactory,
            TestStateRepository testStateRepository
    ) {
        this.testStateFactory = testStateFactory;
        this.testStateRepository = testStateRepository;
    }

    public TestState createTestState(UUID testId, TestStateRequest testStateRequest) {
        // creates a new test state, or if a test state is already present for the given (user, test), the old test state is reset to new settings
        TestState testState = testStateFactory.createTestState(testId, testStateRequest);
        Optional<TestState> oldTestStateOptional = testStateRepository.findFirstByTest_Id(testId);
        if (oldTestStateOptional.isPresent()) {
            testStateRepository.delete(oldTestStateOptional.get());
        }
        return testStateRepository.save(testState);
    }

    public TestState findTestState(UUID stateId) {
        return testStateRepository.findById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test state for test with given id not found: " + stateId));
    }

    public TestState findTestStateByTestId(UUID testId) {
        return testStateRepository.findFirstByTest_Id(testId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test state for test with given id not found: " + testId));
    }

    public void updateTestExecutionState(UUID testStateId) {
        Optional<TestState> testStateOptional = testStateRepository.findById(testStateId);
        if (testStateOptional.isEmpty()) {
            throw new IllegalArgumentException("Unknown test state with id: " + testStateId);
        }
        TestState testState = testStateOptional.get();
        TestExecutionState executionState = testState.getExecutionState();
        if (TestExecutionState.getNext(executionState) == TestExecutionState.NOT_STARTED) {
            // todo: reset test
            testStateFactory.resetTestState(testState);
        }
        if (executionState == TestExecutionState.IN_PROGRESS && isLastQuestionIndex(testState)) {
            if (testState.getMode() == TestMode.LEARNING && notAllQuestionsAnsweredCorrectly(testState)) {
                // todo: change current question index?
                // todo: all question index update should be done HERE and not while
                // todo: updating the question state!
                testState.getQuestions().stream()// todo: update question count of each type?
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
        } else if (executionState == TestExecutionState.IN_PROGRESS) {
            testState.setCurrentQuestionIndex(testState.getCurrentQuestionIndex() + 1);
        }
        testStateRepository.save(testState);
        // todo: if learning mode and not all questions answered correctly, reset the wrong answered questions and currentQuestionIndex

    }

    private boolean isLastQuestionIndex(TestState testState) {
        return testState.getCurrentQuestionIndex() == testState.getQuestions().size() - 1;
    }

    private boolean notAllQuestionsAnsweredCorrectly(TestState testState) {
        return testState.getQuestions().stream()
                .anyMatch(q -> !q.isWasCorrectAnswer());
    }

}
