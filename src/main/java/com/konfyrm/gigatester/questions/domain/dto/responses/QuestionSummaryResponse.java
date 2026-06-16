package com.konfyrm.gigatester.questions.domain.dto.responses;

import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryResponse {

    private UUID id;
    private QuestionType questionType;

}