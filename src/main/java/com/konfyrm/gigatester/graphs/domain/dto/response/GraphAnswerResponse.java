package com.konfyrm.gigatester.graphs.domain.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphAnswerResponse {
    private boolean correct;
    private String vertexId;
    /** Only populated when correct — the now-revealed label. */
    private String label;
    private boolean completed;
}
