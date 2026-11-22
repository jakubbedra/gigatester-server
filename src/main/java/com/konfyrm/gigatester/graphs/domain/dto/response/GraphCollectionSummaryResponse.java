package com.konfyrm.gigatester.graphs.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GraphCollectionSummaryResponse {
    private UUID id;
    private String name;
    private int graphCount;
}
