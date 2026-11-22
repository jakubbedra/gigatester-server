package com.konfyrm.gigatester.graphs.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GraphCollectionRequest {
    private String name;
    /** In play order. */
    private List<UUID> graphIds;
}
