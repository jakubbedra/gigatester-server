package com.konfyrm.gigatester.tests.service;

import com.google.common.collect.ImmutableList;
import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.metrics.domain.entity.UserQuestionStat;
import com.konfyrm.gigatester.metrics.repository.UserQuestionStatRepository;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.tests.domain.converter.TestDisplayTypeToDtoConverter;
import com.konfyrm.gigatester.tests.domain.converter.TestModeToDtoConverter;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestQuestionDistributionMode;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.*;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TestStateFactory {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final UserQuestionStatRepository userQuestionStatRepository;

    @Autowired
    public TestStateFactory(
            TestRepository testRepository,
            QuestionRepository questionRepository,
            UserQuestionStatRepository userQuestionStatRepository
    ) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.userQuestionStatRepository = userQuestionStatRepository;
    }

    public TestState createTestState(UUID testId, TestStateRequest request, User user) {
        Optional<Test> testOptional = testRepository.findById(testId);
        if (testOptional.isEmpty()) {
            throw new IllegalArgumentException("Test with id: " + testId + " not found.");
        }
        TestModeDto mode = request.getMode();
        TestState.TestStateBuilder builder = TestState.builder()
                .test(testOptional.get())
                .currentQuestionIndex(0)
                .startTime(System.currentTimeMillis())
                .executionState(TestExecutionState.IN_PROGRESS)
                .mode(TestModeToDtoConverter.toEntity(mode))
                .closedQuestionsCount(request.getClosedQuestionsCount())
                .openQuestionsCount(request.getOpenQuestionsCount())
                .statementQuestionsCount(request.getStatementQuestionsCount())
                .displayType(TestDisplayTypeToDtoConverter.toEntity(request.getDisplayType()))
                .timeLimitEnabled(request.isTimeLimitEnabled())
                .timeLimitMs(request.getTimeLimitMs());
        if (request.getPassingPercentage() != null) {
            builder.passingPercentage(request.getPassingPercentage());
        }
        return createQuestionStates(testId, builder, request, user).build();
    }

    public void resetTestState(TestState testState) {
        testState.setCurrentQuestionIndex(0);
        testState.setExecutionState(TestExecutionState.IN_PROGRESS);
        testState.setStartTime(System.currentTimeMillis());
        testState.setCumulativeAttempted(0);
        testState.setCumulativeCorrect(0);
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

    private TestState.TestStateBuilder createQuestionStates(UUID testId, TestState.TestStateBuilder builder, TestStateRequest request, User user) {
        List<UUID> tagIds = request.getTagIds();
        boolean exclude = request.isExcludeTags();
        boolean matchAll = request.isMatchAllTags();
        TestQuestionDistributionMode distributionMode = request.getDistributionMode() != null
                ? request.getDistributionMode() : TestQuestionDistributionMode.RANDOM;
        int maxPerTag = request.getMaxPerTag();
        if (request.getMode() == TestModeDto.LEARNING) {
            List<QuestionState> closedQuestionStates = fetchQuestions(testId, TesterEntityType.CLOSED_QUESTION, tagIds, exclude, matchAll, distributionMode, maxPerTag, user);
            List<QuestionState> openQuestionStates = fetchQuestions(testId, TesterEntityType.OPEN_QUESTION, tagIds, exclude, matchAll, distributionMode, maxPerTag, user);
            List<QuestionState> statementQuestionStates = fetchQuestions(testId, TesterEntityType.STATEMENT_QUESTION, tagIds, exclude, matchAll, distributionMode, maxPerTag, user);
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
                .addAll(fetchQuestions(testId, TesterEntityType.CLOSED_QUESTION, request.getClosedQuestionsCount(), tagIds, exclude, matchAll, distributionMode, maxPerTag, user))
                .addAll(fetchQuestions(testId, TesterEntityType.OPEN_QUESTION, request.getOpenQuestionsCount(), tagIds, exclude, matchAll, distributionMode, maxPerTag, user))
                .addAll(fetchQuestions(testId, TesterEntityType.STATEMENT_QUESTION, request.getStatementQuestionsCount(), tagIds, exclude, matchAll, distributionMode, maxPerTag, user))
                .build());
        Collections.shuffle(questionStates);
        int order = 0;
        for (QuestionState question : questionStates) {
            question.setSequence(order++);
        }
        return builder.questions(questionStates);
    }

    private List<Question> fetchCandidatePool(UUID testId, TesterEntityType entityType, List<UUID> tagIds, boolean exclude, boolean matchAll) {
        if (tagIds == null || tagIds.isEmpty()) {
            return questionRepository.findRandomQuestions(testId, entityType.toString());
        } else if (exclude && matchAll) {
            return questionRepository.findRandomQuestionsExcludingAllTags(testId, entityType.toString(), tagIds, tagIds.size());
        } else if (exclude) {
            return questionRepository.findRandomQuestionsExcludingTags(testId, entityType.toString(), tagIds);
        } else if (matchAll) {
            return questionRepository.findRandomQuestionsByAllTags(testId, entityType.toString(), tagIds, tagIds.size());
        } else {
            return questionRepository.findRandomQuestionsByTags(testId, entityType.toString(), tagIds);
        }
    }

    private List<QuestionState> fetchQuestions(UUID testId, TesterEntityType entityType, List<UUID> tagIds, boolean exclude, boolean matchAll,
                                                TestQuestionDistributionMode distributionMode, int maxPerTag, User user) {
        QuestionStateCreationStrategy strategy = QuestionStateCreationStrategy.getStrategy(entityType);
        List<Question> pool = fetchCandidatePool(testId, entityType, tagIds, exclude, matchAll);
        List<Question> distributed = distributionMode == TestQuestionDistributionMode.WORST
                ? sortByWorst(pool, user)
                : QuestionDistributionUtil.apply(pool, tagIds, exclude, distributionMode, maxPerTag);
        return distributed.stream().map(strategy::createQuestionState).toList();
    }

    private List<QuestionState> fetchQuestions(UUID testId, TesterEntityType entityType, int count, List<UUID> tagIds, boolean exclude, boolean matchAll,
                                                TestQuestionDistributionMode distributionMode, int maxPerTag, User user) {
        QuestionStateCreationStrategy strategy = QuestionStateCreationStrategy.getStrategy(entityType);
        List<Question> pool = fetchCandidatePool(testId, entityType, tagIds, exclude, matchAll);
        List<Question> distributed = distributionMode == TestQuestionDistributionMode.WORST
                ? sortByWorst(pool, user)
                : QuestionDistributionUtil.apply(pool, tagIds, exclude, distributionMode, maxPerTag);
        List<Question> limited = distributed.size() > count ? distributed.subList(0, count) : distributed;
        return limited.stream().map(strategy::createQuestionState).toList();
    }

    /**
     * Orders candidates by this user's error rate on them, worst first, so a later
     * subList(0, count) picks "the X questions I get wrong the most". Questions never
     * attempted rank last (there's no evidence they're a weak spot), below any question
     * with a genuine error rate — even 0%.
     */
    private List<Question> sortByWorst(List<Question> pool, User user) {
        List<UUID> ids = pool.stream().map(Question::getId).toList();
        Map<UUID, UserQuestionStat> statsByQuestionId = userQuestionStatRepository
                .findByUser_IdAndQuestion_IdIn(user.getId(), ids).stream()
                .collect(Collectors.toMap(s -> s.getQuestion().getId(), s -> s));
        return pool.stream()
                .sorted(Comparator.comparingDouble((Question q) -> errorRate(statsByQuestionId.get(q.getId()))).reversed())
                .collect(Collectors.toList());
    }

    private double errorRate(UserQuestionStat stat) {
        if (stat == null || stat.getTimesAnswered() == 0) return -1.0;
        return (double) (stat.getTimesAnswered() - stat.getTimesCorrect()) / stat.getTimesAnswered();
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
