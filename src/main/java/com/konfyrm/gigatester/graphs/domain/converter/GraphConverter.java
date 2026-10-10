package com.konfyrm.gigatester.graphs.domain.converter;

import com.konfyrm.gigatester.graphs.domain.dto.request.GraphRequest;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphResponse;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphSummaryResponse;
import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraphConverter {

    public Graph toEntity(GraphRequest request) {
        return Graph.builder()
                .name(request.getName())
                .graphJson(request.getGraphJson())
                .build();
    }

    public GraphResponse toResponse(Graph graph) {
        return GraphResponse.builder()
                .id(graph.getId())
                .name(graph.getName())
                .graphJson(graph.getGraphJson())
                .build();
    }

    public List<GraphSummaryResponse> toSummaryResponses(List<Graph> graphs) {
        return graphs.stream()
                .map(g -> GraphSummaryResponse.builder().id(g.getId()).name(g.getName()).build())
                .toList();
    }

}
