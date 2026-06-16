package com.konfyrm.gigatester.questions.domain.dto;

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
public class StatementQuestionDto extends QuestionDto {

    private List<StatementDto> statements;

    private Double points = 1.0;

}