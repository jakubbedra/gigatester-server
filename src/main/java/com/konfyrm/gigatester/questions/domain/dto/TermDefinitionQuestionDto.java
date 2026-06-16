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
public class TermDefinitionQuestionDto extends QuestionDto {

    private List<TermDefinitionPairDto> termDefinitions;

    private Double points;

}