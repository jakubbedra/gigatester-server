package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.TermDefinitionQuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.domain.entity.TermDefinitionQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TermDefinitionQuestionConverter extends AbstractQuestionConverter<TermDefinitionQuestionDto, TermDefinitionQuestion> {

    private final TermDefinitionPairConverter termDefinitionPairConverter;

    @Autowired
    public TermDefinitionQuestionConverter(
            QuestionContentConverter questionContentConverter
    ) {
        super(TesterEntityType.TERM_DEFINITION_QUESTION, QuestionType.TERM_DEFINITION, questionContentConverter);
        this.termDefinitionPairConverter = TermDefinitionPairConverter.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Question.QuestionBuilder<TermDefinitionQuestion, ?> createQuestionBuilder(TermDefinitionQuestionDto dto) {
        return (Question.QuestionBuilder<TermDefinitionQuestion, ?>) TermDefinitionQuestion.builder()
                .termDefinitions(dto.getTermDefinitions().stream().map(termDefinitionPairConverter::toEntity).toList())
                .points(dto.getPoints());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected QuestionDto.QuestionDtoBuilder<TermDefinitionQuestionDto, ?> createQuestionDtoBuilder(TermDefinitionQuestion termDefinitionQuestion) {
        return (QuestionDto.QuestionDtoBuilder<TermDefinitionQuestionDto, ?>) TermDefinitionQuestionDto.builder()
                .id(termDefinitionQuestion.getId())
                .termDefinitions(termDefinitionQuestion.getTermDefinitions().stream().map(termDefinitionPairConverter::toDto).toList())
                .points(termDefinitionQuestion.getPoints());
    }

}
