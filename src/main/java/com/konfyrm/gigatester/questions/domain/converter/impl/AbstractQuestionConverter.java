package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import jakarta.annotation.Nonnull;

public abstract class AbstractQuestionConverter<DTO extends QuestionDto, ENTITY extends Question> implements EntityConverter<DTO, ENTITY> {

    private final TesterEntityType entityType;
    private final QuestionType questionType;

    protected final QuestionContentConverter questionContentConverter;

    public AbstractQuestionConverter(
            TesterEntityType entityType,
            QuestionType questionType,
            QuestionContentConverter questionContentConverter
    ) {
        this.questionContentConverter = questionContentConverter;
        this.entityType = entityType;
        this.questionType = questionType;
    }

    @Nonnull
    @Override
    public ENTITY toEntity(@Nonnull DTO dto) {
        Question.QuestionBuilder<ENTITY, ?> questionBuilder = createQuestionBuilder(dto);
        if (dto.getId() != null) {
            questionBuilder.id(dto.getId());
        }
        return questionBuilder
                .type(entityType)
                .content(questionContentConverter.toEntity(dto.getContent()))
                .contentProportions(dto.getContentProportions())
                .answerProportions(dto.getAnswerProportions())
                .build();
    }

    @Nonnull
    @Override
    public DTO toDto(@Nonnull ENTITY entity) {
        return createQuestionDtoBuilder(entity)
                .type(questionType)
                .content(questionContentConverter.toDto(entity.getContent()))
                .contentProportions(entity.getContentProportions())
                .answerProportions(entity.getAnswerProportions())
                .build();
    }

    protected abstract Question.QuestionBuilder<ENTITY, ?> createQuestionBuilder(DTO dto);

    protected abstract QuestionDto.QuestionDtoBuilder<DTO, ?> createQuestionDtoBuilder(ENTITY entity);

}