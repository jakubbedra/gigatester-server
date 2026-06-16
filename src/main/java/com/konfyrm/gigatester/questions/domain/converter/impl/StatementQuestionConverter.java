package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.StatementQuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.StatementQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatementQuestionConverter extends AbstractQuestionConverter<StatementQuestionDto, StatementQuestion> {

    private final StatementConverter statementConverter;

    @Autowired
    public StatementQuestionConverter(
            QuestionContentConverter questionContentConverter,
            StatementConverter statementConverter
    ) {
        super(TesterEntityType.STATEMENT_QUESTION, QuestionType.STATEMENT, questionContentConverter);
        this.statementConverter = statementConverter;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected StatementQuestion.StatementQuestionBuilder<StatementQuestion, ?> createQuestionBuilder(StatementQuestionDto dto) {
        return (StatementQuestion.StatementQuestionBuilder<StatementQuestion, ?>) StatementQuestion.builder()
                .statements(dto.getStatements().stream().map(statementConverter::toEntity).toList())
                .points(dto.getPoints());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected QuestionDto.QuestionDtoBuilder<StatementQuestionDto, ?> createQuestionDtoBuilder(StatementQuestion question) {
        return (StatementQuestionDto.StatementQuestionDtoBuilder<StatementQuestionDto, ?>) StatementQuestionDto.builder()
                .statements(question.getStatements().stream().map(statementConverter::toDto).toList())
                .points(question.getPoints());
    }

}
