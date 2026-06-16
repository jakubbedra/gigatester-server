package com.konfyrm.gigatester.questions.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTagListDto {

    private List<QuestionTagDto> questionTags;

}