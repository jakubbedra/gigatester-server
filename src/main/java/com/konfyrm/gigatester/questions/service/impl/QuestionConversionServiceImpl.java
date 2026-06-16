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
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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

    @Autowired
    public QuestionConversionServiceImpl(
            ClosedQuestionConverter closedQuestionMapper,
            OpenQuestionConverter openQuestionConverter,
            StatementQuestionConverter statementQuestionConverter
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
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public Question toEntity(@Nonnull QuestionDto questionDto) {
        AbstractQuestionConverter<QuestionDto, ?> questionMapper = (AbstractQuestionConverter<QuestionDto, ?>) questionTypeToMapper.get(questionDto.getType());
        if (questionMapper == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported question type: " + questionDto.getType());
        }
        return questionMapper.toEntity(questionDto);
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