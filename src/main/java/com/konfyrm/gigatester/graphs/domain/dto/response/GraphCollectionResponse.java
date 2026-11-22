package com.konfyrm.gigatester.graphs.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/** Everything about one collection (editor view), and, for players, only the graphs they may open (play view). */
@Getter
@Builder
public class GraphCollectionResponse {
    private UUID id;
    private String name;
    private List<GraphSummaryResponse> graphs;
}
