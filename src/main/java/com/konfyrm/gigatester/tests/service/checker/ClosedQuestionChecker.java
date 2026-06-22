package com.konfyrm.gigatester.tests.service.checker;

import com.konfyrm.gigatester.questions.domain.entity.ClosedQuestion;
import com.konfyrm.gigatester.questions.domain.entity.ClosedQuestionAnswer;
import com.konfyrm.gigatester.questions.domain.entity.enums.MultipleChoiceScoringMode;
import com.konfyrm.gigatester.tests.domain.dto.request.ClosedQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.ClosedQuestionState;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ClosedQuestionChecker implements QuestionChecker {

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        if (!(state instanceof ClosedQuestionState closedQuestionState)) {
            throw new IllegalArgumentException("ClosedQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (!(request instanceof ClosedQuestionStateRequest stateRequest)) {
            throw new IllegalArgumentException("ClosedQuestionChecker called for invalid state request class: " + request.getClass());
        }
        closedQuestionState.setSelectedAnswerIds(stateRequest.getSelectedAnswers());
        return check(closedQuestionState, request.isAnswered());
    }

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, boolean answered) {
        if (!(state instanceof ClosedQuestionState closedQuestionState)) {
            throw new IllegalArgumentException("ClosedQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (state.getQuestion() instanceof ClosedQuestion closedQuestion) {
//            MultipleChoiceScoringMode scoringMode = closedQuestion.getScoringMode();
            Set<UUID> selectedAnswers = closedQuestionState.getSelectedAnswerIds();
            state.setAnswered(answered);
            closedQuestionState.setSelectedAnswerIds(selectedAnswers);
            if (state.isAnswered()) {
                if (closedQuestion.getAnswers().stream()
                        .anyMatch(a -> !a.isCorrect() && selectedAnswers.contains(a.getId()))) {
                    state.setScore(0.0);
                    state.setWasCorrectAnswer(false);
                    return state;
                }
                if (closedQuestion.getAnswers().stream()
                        .anyMatch(a -> a.isCorrect() && !selectedAnswers.contains(a.getId()))) {
                    state.setScore(0.0);
                    state.setWasCorrectAnswer(false);
                    return state;
                }
                state.setScore(closedQuestion.getPoints());
                state.setWasCorrectAnswer(true);
            }
            return state;
        }
        throw new IllegalArgumentException("ClosedQuestionChecker called for invalid question class: " + state.getQuestion().getClass());
    }

    @Override
    public boolean hasConflict(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        return state.isAnswered();
    }

    private ClosedQuestionState updateScore(ClosedQuestionState state, ClosedQuestion closedQuestion, Set<UUID> selectedAnswers) {
        Double score = closedQuestion.getAnswers().stream()
                .filter(a -> !a.isCorrect() && selectedAnswers.contains(a.getId()))
                .map(ClosedQuestionAnswer::getPoints)
                .reduce(Double::sum)
                .orElse(0.0);
        state.setScore(score);
        state.setWasCorrectAnswer(Objects.equals(state.getScore(), closedQuestion.getPoints()) || Objects.equals(
                state.getScore(), closedQuestion.getAnswers().stream()
                        .map(ClosedQuestionAnswer::getPoints)
                        .reduce(Double::sum)
                        .orElseThrow(() -> new IllegalArgumentException("Question with no answers detected: " + closedQuestion.getId())))
        );
        return state;
    }

    private boolean isStrictScoring(ClosedQuestion closedQuestion, MultipleChoiceScoringMode scoringMode) {
        return scoringMode == MultipleChoiceScoringMode.STRICT || scoringMode == MultipleChoiceScoringMode.UNDEFINED && !closedQuestion.isMultipleChoice();
    }

}
