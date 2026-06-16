package com.konfyrm.gigatester.questions.domain.dto;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedQuestionDto extends QuestionDto {

    @Nonnull
    private List<ClosedQuestionAnswerDto> answers;

    @Nonnull
    private Boolean multipleChoice;

    @Nullable
    private Double points;

}
