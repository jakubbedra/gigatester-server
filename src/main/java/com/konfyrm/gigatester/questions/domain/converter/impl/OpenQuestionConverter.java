package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.OpenQuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionContentDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.domain.entity.QuestionContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                .points(dto.getPoints())
                .multipleAnswers(dto.isMultipleAnswers())
                .orderedAnswers(dto.isOrderedAnswers());
        if (dto.getAnswerVariations() != null) {
            List<QuestionContentDto> variations = dto.getAnswerVariations();
            builder.answerVariations(IntStream.range(0, variations.size())
                    .mapToObj(i -> {
                        QuestionContent entity = questionContentConverter.toEntity(variations.get(i));
                        entity.setVariationOrder(i);
                        return entity;
                    })
                    .collect(Collectors.toSet()));
        }
        return (Question.QuestionBuilder<OpenQuestion, ?>) builder;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected QuestionDto.QuestionDtoBuilder<OpenQuestionDto, ?> createQuestionDtoBuilder(OpenQuestion openQuestion) {
        return (QuestionDto.QuestionDtoBuilder<OpenQuestionDto, ?>) OpenQuestionDto.builder()
                .answer(questionContentConverter.toDto(openQuestion.getAnswer()))
                .answerVariations(openQuestion.getAnswerVariations().stream()
                        .sorted(Comparator.comparing(QuestionContent::getVariationOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(questionContentConverter::toDto)
                        .collect(Collectors.toList()))
                .gradingRules(GradingRule.fromHash(openQuestion.getGradingRulesHash()))
                .points(openQuestion.getPoints())
                .multipleAnswers(Boolean.TRUE.equals(openQuestion.getMultipleAnswers()))
                .orderedAnswers(Boolean.TRUE.equals(openQuestion.getOrderedAnswers()));
    }

}