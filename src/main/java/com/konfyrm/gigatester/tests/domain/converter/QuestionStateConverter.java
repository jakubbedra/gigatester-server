package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import jakarta.annotation.Nonnull;

public interface QuestionStateConverter {

    @Nonnull
    QuestionStateResponse.QuestionStateResponseBuilder<?, ?> toResponseBuilder(@Nonnull QuestionState entity);

    @Nonnull
    QuestionState.QuestionStateBuilder<?, ?> toEntityBuilder(@Nonnull QuestionStateRequest request);

}