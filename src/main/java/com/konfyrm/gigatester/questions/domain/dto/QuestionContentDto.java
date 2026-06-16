package com.konfyrm.gigatester.questions.domain.dto;

import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionContentType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContentDto {

    private QuestionContentType type;

    private String text;

}
