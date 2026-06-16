package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.ClosedQuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.ClosedQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClosedQuestionConverter extends AbstractQuestionConverter<ClosedQuestionDto, ClosedQuestion> {

    private final ClosedQuestionAnswerConverter closedQuestionAnswerConverter;

    @Autowired
    public ClosedQuestionConverter(
            QuestionContentConverter questionContentConverter,
            ClosedQuestionAnswerConverter closedQuestionAnswerConverter
    ) {
        super(TesterEntityType.CLOSED_QUESTION, QuestionType.CLOSED, questionContentConverter);
        this.closedQuestionAnswerConverter = closedQuestionAnswerConverter;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ClosedQuestion.ClosedQuestionBuilder<ClosedQuestion, ?> createQuestionBuilder(ClosedQuestionDto dto) {
        return (ClosedQuestion.ClosedQuestionBuilder<ClosedQuestion, ?>) ClosedQuestion.builder()
                .answers(dto.getAnswers().stream().map(closedQuestionAnswerConverter::toEntity).toList())
                .multipleChoice(dto.getMultipleChoice())
                .points(dto.getPoints());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected QuestionDto.QuestionDtoBuilder<ClosedQuestionDto, ?> createQuestionDtoBuilder(ClosedQuestion closedQuestion) {
        return (QuestionDto.QuestionDtoBuilder<ClosedQuestionDto, ?>) ClosedQuestionDto.builder()
                .answers(closedQuestion.getAnswers().stream().map(closedQuestionAnswerConverter::toDto).toList())
                .multipleChoice(closedQuestion.isMultipleChoice())
                .points(closedQuestion.getPoints());
    }

}
