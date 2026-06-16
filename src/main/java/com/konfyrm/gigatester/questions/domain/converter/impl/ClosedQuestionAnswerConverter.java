package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.questions.domain.dto.ClosedQuestionAnswerDto;
import com.konfyrm.gigatester.questions.domain.entity.ClosedQuestionAnswer;
import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component
public class ClosedQuestionAnswerConverter implements EntityConverter<ClosedQuestionAnswerDto, ClosedQuestionAnswer> {

    @Nonnull
    @Override
    public ClosedQuestionAnswer toEntity(@Nonnull ClosedQuestionAnswerDto closedQuestionAnswerDto) {
        return ClosedQuestionAnswer.builder()
                .text(closedQuestionAnswerDto.getText())
                .correct(closedQuestionAnswerDto.getCorrect())
                .points(closedQuestionAnswerDto.getPoints())
                .build();
    }

    @Nonnull
    @Override
    public ClosedQuestionAnswerDto toDto(@Nonnull ClosedQuestionAnswer closedQuestionAnswer) {
        return ClosedQuestionAnswerDto.builder()
                .id(closedQuestionAnswer.getId())
                .text(closedQuestionAnswer.getText())
                .correct(closedQuestionAnswer.isCorrect())
                .points(closedQuestionAnswer.getPoints())
                .build();
    }

}