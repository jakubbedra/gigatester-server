package com.konfyrm.gigatester.tests.service.checker;

import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.tests.domain.dto.request.OpenQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.OpenQuestionState;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import jakarta.annotation.Nonnull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OpenQuestionChecker implements QuestionChecker {

    private static final String WHITE_SPACE_REGEX = "\\s+";
    private static final String PUNCTUATION_REGEX = "\\p{Punct}+";

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        if (!(state instanceof OpenQuestionState openQuestionState)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (!(request instanceof OpenQuestionStateRequest openQuestionStateRequest)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid state request class: " + request.getClass());
        }
        if (!(state.getQuestion() instanceof OpenQuestion openQuestion)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid question class: " + state.getQuestion().getClass());
        }
        openQuestionState.setGivenAnswer(openQuestionStateRequest.getGivenAnswer());
        state.setAnswered(request.isAnswered());
        Set<GradingRule> gradingRules = GradingRule.fromHash(openQuestion.getGradingRulesHash());
        if (gradingRules.contains(GradingRule.MANUAL)) {
            if (!state.isAnswered()) {
                return state;
            }
            state.setScore(openQuestionStateRequest.getScoredPoints());
            state.setWasCorrectAnswer(Objects.equals(state.getScore(), openQuestion.getPoints()));
            return state;
        }

        return check(state, openQuestion, gradingRules, openQuestionStateRequest.getGivenAnswer());
    }

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, boolean answered) {
        if (!(state instanceof OpenQuestionState openQuestionState)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (!(state.getQuestion() instanceof OpenQuestion openQuestion)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid question class: " + state.getQuestion().getClass());
        }
        Set<GradingRule> gradingRules = GradingRule.fromHash(openQuestion.getGradingRulesHash());
        state.setAnswered(answered);
        return check(state, openQuestion, gradingRules, openQuestionState.getGivenAnswer());
    }

    @SuppressWarnings("all")
    @Override
    public boolean hasConflict(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        if (!(state instanceof OpenQuestionState openQuestionState)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (!(request instanceof OpenQuestionStateRequest openQuestionStateRequest)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid state request class: " + request.getClass());
        }
        if (!(state.getQuestion() instanceof OpenQuestion openQuestion)) {
            throw new IllegalArgumentException("OpenQuestionChecker called for invalid question class: " + state.getQuestion().getClass());
        }
        Set<GradingRule> gradingRules = GradingRule.fromHash(openQuestion.getGradingRulesHash());
        if (gradingRules.contains(GradingRule.MANUAL)) {
            return openQuestionState.isAnswered() && !openQuestionStateRequest.getGivenAnswer().equals(openQuestionState.getGivenAnswer());
        }
        return openQuestionState.isAnswered();
    }

    private QuestionState check(@Nonnull QuestionState state, OpenQuestion openQuestion, Set<GradingRule> gradingRules, String givenAnswer) {
        if (Boolean.TRUE.equals(openQuestion.getMultipleAnswers())) {
            return checkMultiple(state, openQuestion, gradingRules, givenAnswer);
        }
        String correctAnswer = openQuestion.getAnswer().getText();
        if (isCorrect(givenAnswer, correctAnswer, gradingRules)) {
            state.setScore(openQuestion.getPoints());
            state.setWasCorrectAnswer(true);
            return state;
        }
        if (openQuestion.getAnswerVariations().stream().anyMatch(answer -> isCorrect(givenAnswer, answer.getText(), gradingRules))) {
            state.setScore(openQuestion.getPoints());
            state.setWasCorrectAnswer(true);
            return state;
        }
        state.setScore(0.0);
        state.setWasCorrectAnswer(false);
        return state;
    }

    private QuestionState checkMultiple(@Nonnull QuestionState state, OpenQuestion openQuestion, Set<GradingRule> gradingRules, String givenAnswer) {
        List<String> required = new java.util.ArrayList<>();
        required.add(openQuestion.getAnswer().getText());
        openQuestion.getAnswerVariations().stream()
                .sorted(Comparator.comparing(
                        com.konfyrm.gigatester.questions.domain.entity.QuestionContent::getVariationOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(v -> required.add(v.getText()));

        List<String> given = givenAnswer == null ? List.of() :
                java.util.Arrays.stream(givenAnswer.split("\n"))
                        .map(String::trim)
                        .collect(java.util.stream.Collectors.toList());

        boolean allCorrect;
        if (Boolean.TRUE.equals(openQuestion.getOrderedAnswers())) {
            allCorrect = required.size() == given.size();
            for (int i = 0; allCorrect && i < required.size(); i++) {
                allCorrect = isCorrect(given.get(i), required.get(i), gradingRules);
            }
        } else {
            List<String> remaining = new java.util.ArrayList<>(required);
            for (String g : given) {
                if (!g.isEmpty()) remaining.removeIf(r -> isCorrect(g, r, gradingRules));
            }
            allCorrect = remaining.isEmpty();
        }

        state.setScore(allCorrect ? openQuestion.getPoints() : 0.0);
        state.setWasCorrectAnswer(allCorrect);
        return state;
    }

    private boolean isCorrect(String givenAnswer, String correctAnswer, Set<GradingRule> gradingRules) {
        if (gradingRules.contains(GradingRule.IGNORE_CASE)) {
            givenAnswer = givenAnswer.toLowerCase();
            correctAnswer = correctAnswer.toLowerCase();
        }
        if (gradingRules.contains(GradingRule.IGNORE_PUNCTUATION)) {
            givenAnswer = givenAnswer.replaceAll(PUNCTUATION_REGEX, "");
            correctAnswer = correctAnswer.replaceAll(PUNCTUATION_REGEX, "");
        }
        if (gradingRules.contains(GradingRule.TRIM_WHITESPACE)) {
            givenAnswer = givenAnswer.replaceAll(WHITE_SPACE_REGEX, "");
            correctAnswer = correctAnswer.replaceAll(WHITE_SPACE_REGEX, "");
        }
        return correctAnswer.equals(givenAnswer);
    }

}
