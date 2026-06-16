package com.konfyrm.gigatester.questions.domain.dto;

import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OpenQuestionDto extends QuestionDto {

    private QuestionContentDto answer;

    @Nullable
    private Set<QuestionContentDto> answerVariations;

    private Set<GradingRule> gradingRules;

    private double points;

}
