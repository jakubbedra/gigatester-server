package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.dto.ArticleReferenceDto;
import com.konfyrm.gigatester.questions.domain.entity.ArticleReference;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import com.konfyrm.gigatester.tags.dto.TagResponse;
import jakarta.annotation.Nonnull;

import java.util.List;

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
                .articleRefs(dto.getArticleRefs() == null ? new java.util.ArrayList<>() : dto.getArticleRefs().stream()
                        .filter(r -> r.getArticleId() != null)
                        .map(r -> ArticleReference.builder().articleId(r.getArticleId())
                                .section(r.getSection() == null || r.getSection().isBlank() ? null : r.getSection().trim()).build())
                        .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)))
                .build();
    }

    @Nonnull
    @Override
    public DTO toDto(@Nonnull ENTITY entity) {
        List<TagResponse> tags = entity.getTags() == null ? List.of() : entity.getTags().stream()
                .map(t -> TagResponse.builder().id(t.getId()).key(t.getKey()).build())
                .toList();
        return createQuestionDtoBuilder(entity)
                .id(entity.getId())
                .type(questionType)
                .content(questionContentConverter.toDto(entity.getContent()))
                .contentProportions(entity.getContentProportions())
                .answerProportions(entity.getAnswerProportions())
                .tags(tags)
                .articleRefs(entity.getArticleRefs() == null ? List.of() : entity.getArticleRefs().stream()
                        .map(r -> new ArticleReferenceDto(r.getArticleId(), r.getSection())).toList())
                .build();
    }

    protected abstract Question.QuestionBuilder<ENTITY, ?> createQuestionBuilder(DTO dto);

    protected abstract QuestionDto.QuestionDtoBuilder<DTO, ?> createQuestionDtoBuilder(ENTITY entity);

}