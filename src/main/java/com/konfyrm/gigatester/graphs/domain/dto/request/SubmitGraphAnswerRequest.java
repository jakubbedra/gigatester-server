package com.konfyrm.gigatester.graphs.domain.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitGraphAnswerRequest {
    private String vertexId;
    private String guess;
}
