package com.konfyrm.gigatester.tests.domain.dto.request;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OpenQuestionStateRequest extends QuestionStateRequest {

    private String givenAnswer;

    @Nullable
    private Double scoredPoints;

}