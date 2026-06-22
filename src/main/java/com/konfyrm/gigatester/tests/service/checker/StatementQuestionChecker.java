package com.konfyrm.gigatester.tests.service.checker;

import com.konfyrm.gigatester.questions.domain.entity.Statement;
import com.konfyrm.gigatester.questions.domain.entity.StatementQuestion;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.StatementQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.StatementQuestionState;
import jakarta.annotation.Nonnull;

import java.util.List;

public class StatementQuestionChecker implements QuestionChecker {

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        if (!(state instanceof StatementQuestionState statementQuestionState)) {
            throw new IllegalArgumentException("StatementQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (!(request instanceof StatementQuestionStateRequest statementQuestionStateRequest)) {
            throw new IllegalArgumentException("StatementQuestionChecker called for invalid state request class: " + request.getClass());
        }
        if (!(state.getQuestion() instanceof StatementQuestion statementQuestion)) {
            throw new IllegalArgumentException("StatementQuestionChecker called for invalid question class: " + state.getQuestion().getClass());
        }
        List<Boolean> answers = statementQuestionStateRequest.getAnswers();
        statementQuestionState.setAnswers(answers);
        state.setAnswered(true);
        List<Statement> statements = statementQuestion.getStatements();
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i) == null || statements.get(i).isAnswer() != answers.get(i)) {
                state.setScore(0.0);
                state.setWasCorrectAnswer(false);
                return state;
            }
        }
        state.setScore(statementQuestion.getPoints());
        state.setWasCorrectAnswer(true);
        return state;
    }

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, boolean answered) {
        return null;
    }

    @Nonnull
    @Override
    public boolean hasConflict(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        return state.isAnswered();
    }

}
