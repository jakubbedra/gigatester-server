package com.konfyrm.gigatester.tests.service;

import com.google.common.collect.ImmutableList;
import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.tests.domain.converter.TestDisplayTypeToDtoConverter;
import com.konfyrm.gigatester.tests.domain.converter.TestModeToDtoConverter;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.*;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TestStateFactory {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public TestStateFactory(
            TestRepository testRepository,
            QuestionRepository questionRepository
    ) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
    }

    public TestState createTestState(UUID testId, TestStateRequest request) {
        Optional<Test> testOptional = testRepository.findById(testId);
        if (testOptional.isEmpty()) {
            throw new IllegalArgumentException("Test with id: " + testId + " not found.");
        }
        TestModeDto mode = request.getMode();
        TestState.TestStateBuilder builder = TestState.builder()
                .test(testOptional.get())
                .currentQuestionIndex(0)
                .executionState(TestExecutionState.IN_PROGRESS)
                .mode(TestModeToDtoConverter.toEntity(mode))
                .displayType(TestDisplayTypeToDtoConverter.toEntity(request.getDisplayType()));
        if (request.getPassingPercentage() != null) {
            builder.passingPercentage(request.getPassingPercentage());
        }
        return createQuestionStates(testId, builder, request).build();
    }

    public void resetTestState(TestState testState) {
        testState.setCurrentQuestionIndex(0);
        testState.setExecutionState(TestExecutionState.IN_PROGRESS);
        resetQuestionStates(testState);
    }

    private void resetQuestionStates(TestState state) {
        UUID testId = state.getTest().getId();
        state.getQuestions().clear();
        if (state.getMode() == TestMode.LEARNING) {
            List<QuestionState> closedQuestionStates = resetQuestionStates(testId, TesterEntityType.CLOSED_QUESTION);
            List<QuestionState> openQuestionStates = resetQuestionStates(testId, TesterEntityType.OPEN_QUESTION);
            List<QuestionState> statementQuestionStates = resetQuestionStates(testId, TesterEntityType.STATEMENT_QUESTION);
            state.getQuestions().addAll(new ArrayList<>(ImmutableList.<QuestionState>builder()
                    .addAll(closedQuestionStates)
                    .addAll(openQuestionStates)
                    .addAll(statementQuestionStates)
                    .build()));
            state.setOpenQuestionsCount(openQuestionStates.size());
            state.setClosedQuestionsCount(closedQuestionStates.size());
            return;
        }
        state.getQuestions().addAll(new ArrayList<>(ImmutableList.<QuestionState>builder()
                .addAll(resetQuestionStates(testId, TesterEntityType.CLOSED_QUESTION, state.getClosedQuestionsCount()))
                .addAll(resetQuestionStates(testId, TesterEntityType.OPEN_QUESTION, state.getOpenQuestionsCount()))
                .addAll(resetQuestionStates(testId, TesterEntityType.STATEMENT_QUESTION, state.getStatementQuestionsCount()))
                .build()));
    }

    private TestState.TestStateBuilder createQuestionStates(UUID testId, TestState.TestStateBuilder builder, TestStateRequest request) {
        if (request.getMode() == TestModeDto.LEARNING) {
            List<QuestionState> closedQuestionStates = resetQuestionStates(testId, TesterEntityType.CLOSED_QUESTION);
            List<QuestionState> openQuestionStates = resetQuestionStates(testId, TesterEntityType.OPEN_QUESTION);
            List<QuestionState> statementQuestionStates = resetQuestionStates(testId, TesterEntityType.STATEMENT_QUESTION);
            return builder
                    .questions(new ArrayList<>(ImmutableList.<QuestionState>builder()
                            .addAll(closedQuestionStates)
                            .addAll(openQuestionStates)
                            .addAll(statementQuestionStates)
                            .build()))
                    .openQuestionsCount(openQuestionStates.size())
                    .closedQuestionsCount(closedQuestionStates.size());
        }
        return builder.questions(new ArrayList<>(ImmutableList.<QuestionState>builder()
                .addAll(resetQuestionStates(testId, TesterEntityType.CLOSED_QUESTION, request.getClosedQuestionsCount()))
                .addAll(resetQuestionStates(testId, TesterEntityType.OPEN_QUESTION, request.getOpenQuestionsCount()))
                .addAll(resetQuestionStates(testId, TesterEntityType.STATEMENT_QUESTION, request.getStatementQuestionsCount()))
                .build()));
    }

    private List<QuestionState> resetQuestionStates(UUID testId, TesterEntityType entityType, int count) {
        QuestionStateCreationStrategy strategy = QuestionStateCreationStrategy.getStrategy(entityType);
        return questionRepository.findRandomQuestions(testId, entityType.toString(), count).stream().map(strategy::createQuestionState).toList();
    }

    private List<QuestionState> resetQuestionStates(UUID testId, TesterEntityType entityType) {
        QuestionStateCreationStrategy strategy = QuestionStateCreationStrategy.getStrategy(entityType);
        return questionRepository.findRandomQuestions(testId, entityType.toString()).stream().map(strategy::createQuestionState).toList();
    }

}
