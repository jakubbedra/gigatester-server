package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.request.ClosedQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.ClosedQuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.ClosedQuestionState;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component(ClosedQuestionStateConverter.QUALIFIER)
public class ClosedQuestionStateConverter implements QuestionStateConverter {

    public static final String QUALIFIER = "closedQuestionStateConverter";

    @Nonnull
    @Override
    public QuestionStateResponse.QuestionStateResponseBuilder<?, ?> toResponseBuilder(@Nonnull QuestionState entity) {
        if (entity instanceof ClosedQuestionState closedQuestionState) {
            return ClosedQuestionStateResponse.builder()
                    .selectedAnswers(closedQuestionState.getSelectedAnswerIds());
        }
        throw new IllegalStateException("OpenQuestionStateConverter called for invalid class: " + entity.getClass().getName());
    }

    @Nonnull
    @Override
    public QuestionState.QuestionStateBuilder<?, ?> toEntityBuilder(@Nonnull QuestionStateRequest request) {
        if (request instanceof ClosedQuestionStateRequest closedQuestionStateRequest) {
            return ClosedQuestionState.builder()
                    .selectedAnswerIds(closedQuestionStateRequest.getSelectedAnswers());
        }
        throw new IllegalStateException("OpenQuestionStateConverter called for invalid class: " + request.getClass().getName());
    }

}
