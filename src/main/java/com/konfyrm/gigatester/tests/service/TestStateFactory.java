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

import java.util.*;

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
                .closedQuestionsCount(request.getClosedQuestionsCount())
                .openQuestionsCount(request.getOpenQuestionsCount())
                .statementQuestionsCount(request.getStatementQuestionsCount())
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
            ArrayList<QuestionState> questionStates = new ArrayList<>(ImmutableList.<QuestionState>builder()
                    .addAll(closedQuestionStates)
                    .addAll(openQuestionStates)
                    .addAll(statementQuestionStates)
                    .build());
            Collections.shuffle(questionStates);
            state.getQuestions().addAll(questionStates);
            int order = 0;
            for (QuestionState question : state.getQuestions()) {
                question.setSequence(order++);
            }
            state.setOpenQuestionsCount(openQuestionStates.size());
            state.setClosedQuestionsCount(closedQuestionStates.size());
            return;
        }
        ArrayList<QuestionState> questionStates = new ArrayList<>(ImmutableList.<QuestionState>builder()
                .addAll(resetQuestionStates(testId, TesterEntityType.CLOSED_QUESTION, state.getClosedQuestionsCount()))
                .addAll(resetQuestionStates(testId, TesterEntityType.OPEN_QUESTION, state.getOpenQuestionsCount()))
                .addAll(resetQuestionStates(testId, TesterEntityType.STATEMENT_QUESTION, state.getStatementQuestionsCount() != null ? state.getStatementQuestionsCount() : 0))
                .build());
        Collections.shuffle(questionStates);
        state.getQuestions().addAll(questionStates);
        int order = 0;
        for (QuestionState question : state.getQuestions()) {
            question.setSequence(order++);
        }
    }

    private TestState.TestStateBuilder createQuestionStates(UUID testId, TestState.TestStateBuilder builder, TestStateRequest request) {
        List<UUID> tagIds = request.getTagIds();
        if (request.getMode() == TestModeDto.LEARNING) {
            List<QuestionState> closedQuestionStates = fetchQuestions(testId, TesterEntityType.CLOSED_QUESTION, tagIds);
            List<QuestionState> openQuestionStates = fetchQuestions(testId, TesterEntityType.OPEN_QUESTION, tagIds);
            List<QuestionState> statementQuestionStates = fetchQuestions(testId, TesterEntityType.STATEMENT_QUESTION, tagIds);
            ArrayList<QuestionState> questionStates = new ArrayList<>(ImmutableList.<QuestionState>builder()
                    .addAll(closedQuestionStates)
                    .addAll(openQuestionStates)
                    .addAll(statementQuestionStates)
                    .build());
            Collections.shuffle(questionStates);
            int order = 0;
            for (QuestionState question : questionStates) {
                question.setSequence(order++);
            }
            return builder
                    .questions(questionStates)
                    .openQuestionsCount(openQuestionStates.size())
                    .closedQuestionsCount(closedQuestionStates.size());
        }
        ArrayList<QuestionState> questionStates = new ArrayList<>(ImmutableList.<QuestionState>builder()
                .addAll(fetchQuestions(testId, TesterEntityType.CLOSED_QUESTION, request.getClosedQuestionsCount(), tagIds))
                .addAll(fetchQuestions(testId, TesterEntityType.OPEN_QUESTION, request.getOpenQuestionsCount(), tagIds))
                .addAll(fetchQuestions(testId, TesterEntityType.STATEMENT_QUESTION, request.getStatementQuestionsCount(), tagIds))
                .build());
        Collections.shuffle(questionStates);
        int order = 0;
        for (QuestionState question : questionStates) {
            question.setSequence(order++);
        }
        return builder.questions(questionStates);
    }

    private List<QuestionState> fetchQuestions(UUID testId, TesterEntityType entityType, List<UUID> tagIds) {
        QuestionStateCreationStrategy strategy = QuestionStateCreationStrategy.getStrategy(entityType);
        List<?> questions = tagIds == null || tagIds.isEmpty()
                ? questionRepository.findRandomQuestions(testId, entityType.toString())
                : questionRepository.findRandomQuestionsByTags(testId, entityType.toString(), tagIds);
        return questions.stream().map(q -> strategy.createQuestionState((com.konfyrm.gigatester.questions.domain.entity.Question) q)).toList();
    }

    private List<QuestionState> fetchQuestions(UUID testId, TesterEntityType entityType, int count, List<UUID> tagIds) {
        QuestionStateCreationStrategy strategy = QuestionStateCreationStrategy.getStrategy(entityType);
        List<?> questions = tagIds == null || tagIds.isEmpty()
                ? questionRepository.findRandomQuestions(testId, entityType.toString(), count)
                : questionRepository.findRandomQuestionsByTags(testId, entityType.toString(), count, tagIds);
        return questions.stream().map(q -> strategy.createQuestionState((com.konfyrm.gigatester.questions.domain.entity.Question) q)).toList();
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
