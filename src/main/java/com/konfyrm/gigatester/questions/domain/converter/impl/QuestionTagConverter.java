package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import com.konfyrm.gigatester.questions.domain.dto.QuestionTagDto;
import com.konfyrm.gigatester.questions.domain.entity.QuestionTag;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component
public class QuestionTagConverter implements EntityConverter<QuestionTagDto, QuestionTag> {

    @Nonnull
    @Override
    public QuestionTag toEntity(@Nonnull QuestionTagDto questionTagDto) {
        return QuestionTag.builder()
                .id(questionTagDto.getId())
                .value(questionTagDto.getValue())
                .build();
    }

    @Nonnull
    @Override
    public QuestionTagDto toDto(@Nonnull QuestionTag questionTag) {
        return QuestionTagDto.builder()
                .id(questionTag.getId())
                .value(questionTag.getValue())
                .build();
    }

}