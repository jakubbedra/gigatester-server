package com.konfyrm.gigatester.tests.domain.dto.response;

import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStateResponse {

    private QuestionType questionType;

    private UUID id;

    private UUID questionId;

    private Double score;

    private boolean answered;

}
