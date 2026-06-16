package com.konfyrm.gigatester.tests.domain.dto.request;

import com.konfyrm.gigatester.questions.domain.dto.TermDefinitionPairDto;
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
public class TermDefinitionQuestionStateRequest extends QuestionStateRequest {

    private List<TermDefinitionPairDto> termDefinitions;

}
