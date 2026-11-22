package com.konfyrm.gigatester.graphs.domain.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphSummaryResponse {
    private UUID id;
    private String name;
    /** True for an image-labelling activity. */
    private boolean image;
}
