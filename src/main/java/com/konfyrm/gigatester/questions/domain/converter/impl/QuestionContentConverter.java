package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.questions.domain.dto.QuestionContentDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionContentType;
import com.konfyrm.gigatester.questions.domain.entity.QuestionContent;
import com.konfyrm.gigatester.questions.domain.entity.enums.ContentType;
import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QuestionContentConverter implements EntityConverter<QuestionContentDto, QuestionContent> {

    private static final Map<QuestionContentType, ContentType> DTO_TO_ENTITY_TYPE = Map.of(
            QuestionContentType.TEXT, ContentType.TEXT,
            QuestionContentType.HTML, ContentType.HTML
    );

    private static final Map<ContentType, QuestionContentType> ENTITY_TO_DTO_TYPE = Map.of(
            ContentType.TEXT, QuestionContentType.TEXT,
            ContentType.HTML, QuestionContentType.HTML
    );

    @Nonnull
    @Override
    public QuestionContent toEntity(@Nonnull QuestionContentDto questionContentDto) {
        return QuestionContent.builder()
                .type(DTO_TO_ENTITY_TYPE.get(questionContentDto.getType()))
                .text(questionContentDto.getText())
                .build();
    }

    @Nonnull
    @Override
    public QuestionContentDto toDto(@Nonnull QuestionContent questionContent) {
        return QuestionContentDto.builder()
                .type(ENTITY_TO_DTO_TYPE.get(questionContent.getType()))
                .text(questionContent.getText())
                .build();
    }

}
