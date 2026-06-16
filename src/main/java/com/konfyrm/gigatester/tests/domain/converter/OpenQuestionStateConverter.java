package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.request.OpenQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.OpenQuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.OpenQuestionState;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component(OpenQuestionStateConverter.QUALIFIER)
public class OpenQuestionStateConverter implements QuestionStateConverter {

    public static final String QUALIFIER = "openQuestionStateConverter";

    @Nonnull
    @Override
    public QuestionStateResponse.QuestionStateResponseBuilder<?, ?> toResponseBuilder(@Nonnull QuestionState entity) {
        if (entity instanceof OpenQuestionState openQuestionState) {
            return OpenQuestionStateResponse.builder()
                    .givenAnswer(openQuestionState.getGivenAnswer());
        }
        throw new IllegalStateException("OpenQuestionStateConverter called for invalid class: " + entity.getClass().getName());
    }

    @Nonnull
    @Override
    public QuestionState.QuestionStateBuilder<?, ?> toEntityBuilder(@Nonnull QuestionStateRequest request) {
        if (request instanceof OpenQuestionStateRequest openQuestionStateRequest) {
            return OpenQuestionState.builder()
                    .givenAnswer(openQuestionStateRequest.getGivenAnswer());
        }
        throw new IllegalStateException("OpenQuestionStateConverter called for invalid class: " + request.getClass().getName());
    }

}
