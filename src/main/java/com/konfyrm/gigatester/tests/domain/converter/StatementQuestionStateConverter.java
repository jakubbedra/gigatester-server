package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.StatementQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.StatementQuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.StatementQuestionState;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component(StatementQuestionStateConverter.QUALIFIER)
public class StatementQuestionStateConverter implements QuestionStateConverter {

    public static final String QUALIFIER = "statementQuestionStateConverter";

    @Nonnull
    @Override
    public QuestionStateResponse.QuestionStateResponseBuilder<?, ?> toResponseBuilder(@Nonnull QuestionState entity) {
        if (entity instanceof StatementQuestionState statementQuestionState) {
            return StatementQuestionStateResponse.builder()
                    .answers(statementQuestionState.getAnswers());
        }
        throw new IllegalStateException("StatementQuestionStateConverter called for invalid class: " + entity.getClass().getName());
    }

    @Nonnull
    @Override
    public QuestionState.QuestionStateBuilder<?, ?> toEntityBuilder(@Nonnull QuestionStateRequest request) {
        if (request instanceof StatementQuestionStateRequest statementQuestionStateRequest) {
            return StatementQuestionState.builder()
                    .answers(statementQuestionStateRequest.getAnswers());
        }
        throw new IllegalStateException("StatementQuestionStateConverter called for invalid class: " + request.getClass().getName());
    }

}
