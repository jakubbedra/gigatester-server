package com.konfyrm.gigatester.tests.service.checker;

import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import jakarta.annotation.Nonnull;

public interface QuestionChecker {

    @Nonnull
    QuestionState check(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request);

}