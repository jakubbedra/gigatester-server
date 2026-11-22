package com.konfyrm.gigatester.graphs.domain.dto.response;

import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphDifficulty;
import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphPlayMode;
import com.konfyrm.gigatester.graphs.domain.json.GraphData;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphSessionResponse {
    private UUID id;
    private UUID graphId;
    private String graphName;
    private GraphDifficulty difficulty;
    private List<GraphVertexView> vertices;
    private List<GraphData.Edge> edges;
    private List<GraphData.Hyperedge> hyperedges;
    private int totalGuessable;
    private int solvedCount;
    private boolean completed;
    private boolean showVertices;
    private String edgeStyle;
    private GraphPlayMode mode;
    /** Image-labelling activity: the picture and its pixel size; null for an ordinary graph. */
    private String imageUrl;
    private Double imageWidth;
    private Double imageHeight;
    /** DRAG_DROP only: labels still to be placed, in a stable shuffled order. Not tied to vertices. */
    private List<String> bank;
}
