package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.OpenQuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OpenQuestionConverter extends AbstractQuestionConverter<OpenQuestionDto, OpenQuestion> {

    @Autowired
    public OpenQuestionConverter(QuestionContentConverter questionContentConverter) {
        super(TesterEntityType.OPEN_QUESTION, QuestionType.OPEN, questionContentConverter);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Question.QuestionBuilder<OpenQuestion, ?> createQuestionBuilder(OpenQuestionDto dto) {
        OpenQuestion.OpenQuestionBuilder<?, ?> builder = OpenQuestion.builder()
                .answer(questionContentConverter.toEntity(dto.getAnswer()))
                .gradingRulesHash(GradingRule.toHash(dto.getGradingRules()))
                .points(dto.getPoints());
        if (dto.getAnswerVariations() != null) {
            builder.answerVariations(dto.getAnswerVariations().stream()
                    .map(questionContentConverter::toEntity)
                    .collect(Collectors.toSet()));
        }
        return (Question.QuestionBuilder<OpenQuestion, ?>) builder;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected QuestionDto.QuestionDtoBuilder<OpenQuestionDto, ?> createQuestionDtoBuilder(OpenQuestion openQuestion) {
        return (QuestionDto.QuestionDtoBuilder<OpenQuestionDto, ?>) OpenQuestionDto.builder()
                .answer(questionContentConverter.toDto(openQuestion.getAnswer()))
                .answerVariations(openQuestion.getAnswerVariations().stream().map(questionContentConverter::toDto).collect(Collectors.toSet()))
                .gradingRules(GradingRule.fromHash(openQuestion.getGradingRulesHash()))
                .points(openQuestion.getPoints());
    }

}