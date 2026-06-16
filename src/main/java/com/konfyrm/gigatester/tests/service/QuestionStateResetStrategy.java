package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.tests.domain.entity.ClosedQuestionState;
import com.konfyrm.gigatester.tests.domain.entity.OpenQuestionState;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.StatementQuestionState;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public enum QuestionStateResetStrategy {
    CLOSED {
        @Override
        public void resetQuestionState(QuestionState state) {
            if (state instanceof ClosedQuestionState closedQuestionState) {
                closedQuestionState.setSelectedAnswerIds(new HashSet<>());
                return;
            }
            throw new IllegalArgumentException("Question with id: " + state.getId() + " is not an ClosedQuestion.");
        }
    },
    OPEN {
        @Override
        public void resetQuestionState(QuestionState state) {
            if (state instanceof OpenQuestionState openQuestionState) {
                openQuestionState.setGivenAnswer("");
                return;
            }
            throw new IllegalArgumentException("Question with id: " + state.getId() + " is not an OpenQuestion.");
        }
    },
    STATEMENT {
        @Override
        protected void resetQuestionState(QuestionState state) {
            if (state instanceof StatementQuestionState statementQuestionState) {
                statementQuestionState.getAnswers().clear();
                return;
            }
            throw new IllegalArgumentException("QuestionState with id: " + state.getId() + " is not a StatementQuestionState.");
        }
    };

    protected abstract void resetQuestionState(QuestionState question);

    public void reset(QuestionState state) {
        state.setAnswered(false);
        state.setWasCorrectAnswer(false);
        state.setScore(0.0);
        resetQuestionState(state);
    }

    private static final Map<TesterEntityType, QuestionStateResetStrategy> STRATEGIES = Map.of(
            TesterEntityType.OPEN_QUESTION, QuestionStateResetStrategy.OPEN,
            TesterEntityType.CLOSED_QUESTION, QuestionStateResetStrategy.CLOSED,
            TesterEntityType.STATEMENT_QUESTION, QuestionStateResetStrategy.STATEMENT
    );

    public static QuestionStateResetStrategy getStrategy(TesterEntityType type) {
        return Optional.ofNullable(STRATEGIES.get(type)).orElseThrow(() -> new IllegalArgumentException("The following entity type has no question creation strategy: " + type));
    }

}