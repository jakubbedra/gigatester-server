package com.konfyrm.gigatester.graphs.domain.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphVertexView {
    private String id;
    private double x;
    private double y;
    /** Null while hidden — never sent to the client until revealed or correctly guessed. */
    private String label;
    private boolean revealed;
}
