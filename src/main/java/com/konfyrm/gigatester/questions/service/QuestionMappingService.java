package com.konfyrm.gigatester.questions.service;

import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.responses.QuestionSummaryResponse;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import jakarta.annotation.Nonnull;

public interface QuestionMappingService {

    @Nonnull
    Question toEntity(@Nonnull QuestionDto questionDto);

    @Nonnull
    QuestionDto toDto(@Nonnull Question question);

    @Nonnull
    QuestionSummaryResponse toSummaryResponse(@Nonnull Question question);

}
