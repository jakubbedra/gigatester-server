package com.konfyrm.gigatester.questions.service.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.converter.impl.AbstractQuestionConverter;
import com.konfyrm.gigatester.questions.domain.converter.impl.ClosedQuestionConverter;
import com.konfyrm.gigatester.questions.domain.converter.impl.OpenQuestionConverter;
import com.konfyrm.gigatester.questions.domain.converter.impl.StatementQuestionConverter;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.dto.responses.QuestionSummaryResponse;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.service.QuestionMappingService;
import com.konfyrm.gigatester.tags.dto.TagResponse;
import com.konfyrm.gigatester.tags.entity.Tag;
import com.konfyrm.gigatester.tags.repository.TagRepository;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service(QuestionConversionServiceImpl.QUALIFIER)
public class QuestionConversionServiceImpl implements QuestionMappingService {

    public static final String QUALIFIER = "questionConversionService";

    private static final Map<TesterEntityType, QuestionType> ENTITY_TO_QUESTION_TYPE = Map.of(
            TesterEntityType.CLOSED_QUESTION, QuestionType.CLOSED,
            TesterEntityType.OPEN_QUESTION, QuestionType.OPEN,
            TesterEntityType.STATEMENT_QUESTION, QuestionType.STATEMENT
    );

    private final Map<QuestionType, AbstractQuestionConverter<? extends QuestionDto, ?>> questionTypeToMapper;
    private final Map<TesterEntityType, AbstractQuestionConverter<? extends QuestionDto, ?>> entityTypeToMapper;
    private final TagRepository tagRepository;

    @Autowired
    public QuestionConversionServiceImpl(
            ClosedQuestionConverter closedQuestionMapper,
            OpenQuestionConverter openQuestionConverter,
            StatementQuestionConverter statementQuestionConverter,
            TagRepository tagRepository
    ) {
        questionTypeToMapper = Map.of(
                QuestionType.CLOSED, closedQuestionMapper,
                QuestionType.OPEN, openQuestionConverter,
                QuestionType.STATEMENT, statementQuestionConverter
        );
        entityTypeToMapper = Map.of(
                TesterEntityType.CLOSED_QUESTION, closedQuestionMapper,
                TesterEntityType.OPEN_QUESTION, openQuestionConverter,
                TesterEntityType.STATEMENT_QUESTION, statementQuestionConverter
        );
        this.tagRepository = tagRepository;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public Question toEntity(@Nonnull QuestionDto questionDto) {
        AbstractQuestionConverter<QuestionDto, ?> questionMapper = (AbstractQuestionConverter<QuestionDto, ?>) questionTypeToMapper.get(questionDto.getType());
        if (questionMapper == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported question type: " + questionDto.getType());
        }
        Question entity = questionMapper.toEntity(questionDto);
        // The type-specific converters never read dto.getTags() — tags live in a separate
        // join table, so resolving/attaching them is handled once here for every question type.
        entity.getTags().addAll(resolveTags(questionDto.getTags()));
        return entity;
    }

    /** Resolves each incoming tag by id, falling back to its key (handy for hand-written bulk-import JSON that only knows tag keys). */
    private List<Tag> resolveTags(List<TagResponse> requestedTags) {
        if (requestedTags == null || requestedTags.isEmpty()) return List.of();

        List<UUID> ids = requestedTags.stream().map(TagResponse::getId).filter(java.util.Objects::nonNull).toList();
        Map<UUID, Tag> byId = tagRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Tag::getId, t -> t));

        List<Tag> resolved = new ArrayList<>();
        for (TagResponse requested : requestedTags) {
            Tag tag = requested.getId() != null ? byId.get(requested.getId()) : null;
            if (tag == null && requested.getKey() != null && !requested.getKey().isBlank()) {
                tag = tagRepository.findByKey(requested.getKey()).orElse(null);
            }
            UUID tagId = tag != null ? tag.getId() : null;
            if (tagId != null && resolved.stream().noneMatch(t -> t.getId().equals(tagId))) {
                resolved.add(tag);
            }
        }
        return resolved;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public QuestionDto toDto(@Nonnull Question question) {
        AbstractQuestionConverter<?, Question> questionMapper = (AbstractQuestionConverter<?, Question>) entityTypeToMapper.get(question.getType());
        if (questionMapper == null) {
            throw new IllegalStateException(String.format("Unsupported type stored in database: %s for question %s", question.getType(), question.getId()));
        }
        return questionMapper.toDto(question);
    }

    @Nonnull
    @Override
    public QuestionSummaryResponse toSummaryResponse(@Nonnull Question question) {
        return QuestionSummaryResponse.builder()
                .id(question.getId())
                .questionType(ENTITY_TO_QUESTION_TYPE.get(question.getType()))
                .build();
    }

}