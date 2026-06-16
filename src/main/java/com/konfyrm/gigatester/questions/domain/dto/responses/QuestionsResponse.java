package com.konfyrm.gigatester.questions.domain.dto.responses;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsResponse {

    private List<QuestionSummaryResponse> questions;

}
