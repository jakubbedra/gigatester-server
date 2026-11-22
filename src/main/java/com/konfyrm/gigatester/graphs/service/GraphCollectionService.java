package com.konfyrm.gigatester.graphs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konfyrm.gigatester.graphs.domain.dto.request.GraphCollectionRequest;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphCollectionResponse;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphCollectionSummaryResponse;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphSummaryResponse;
import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import com.konfyrm.gigatester.graphs.domain.entity.GraphCollection;
import com.konfyrm.gigatester.graphs.repository.GraphCollectionRepository;
import com.konfyrm.gigatester.graphs.repository.GraphRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GraphCollectionService {

    private final GraphCollectionRepository collectionRepository;
    private final GraphRepository graphRepository;
    private final SubjectGroupAccessService accessService;
    private final ObjectMapper objectMapper;

    public GraphCollectionService(GraphCollectionRepository collectionRepository, GraphRepository graphRepository,
                                  SubjectGroupAccessService accessService, ObjectMapper objectMapper) {
        this.collectionRepository = collectionRepository;
        this.graphRepository = graphRepository;
        this.accessService = accessService;
        this.objectMapper = objectMapper;
    }

    /** Staff overview: every collection with its full graph count. */
    @Transactional(readOnly = true)
    public List<GraphCollectionSummaryResponse> listAll() {
        return collectionRepository.findAll().stream()
                .map(c -> summary(c, c.getGraphs().size()))
                .toList();
    }

    /** What a player can start: collections that contain at least one graph they may open. */
    @Transactional(readOnly = true)
    public List<GraphCollectionSummaryResponse> playable(User user) {
        return collectionRepository.findAll().stream()
                .map(c -> summary(c, (int) c.getGraphs().stream().filter(g -> accessService.hasAccessToGraph(g.getId(), user)).count()))
                .filter(s -> s.getGraphCount() > 0)
                .toList();
    }

    /** Editor view: all graphs of the collection, in order. */
    @Transactional(readOnly = true)
    public GraphCollectionResponse get(UUID id) {
        return response(find(id), find(id).getGraphs());
    }

    /** Play view: only the graphs this player may open, in order. */
    @Transactional(readOnly = true)
    public GraphCollectionResponse play(UUID id, User user) {
        GraphCollection collection = find(id);
        List<Graph> allowed = collection.getGraphs().stream()
                .filter(g -> accessService.hasAccessToGraph(g.getId(), user)).toList();
        if (allowed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return response(collection, allowed);
    }

    @Transactional
    public GraphCollection create(GraphCollectionRequest request, User user) {
        GraphCollection collection = GraphCollection.builder().createdBy(user).build();
        apply(collection, request);
        return collectionRepository.save(collection);
    }

    @Transactional
    public void update(UUID id, GraphCollectionRequest request) {
        GraphCollection collection = find(id);
        apply(collection, request);
        collectionRepository.save(collection);
    }

    @Transactional
    public void delete(UUID id) {
        collectionRepository.delete(find(id));
    }

    private GraphCollection find(UUID id) {
        return collectionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(GraphCollection collection, GraphCollectionRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A collection needs a name.");
        }
        collection.setName(request.getName().trim());
        List<Graph> graphs = new ArrayList<>();
        for (UUID graphId : request.getGraphIds() == null ? List.<UUID>of() : request.getGraphIds()) {
            graphs.add(graphRepository.findById(graphId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown graph " + graphId)));
        }
        collection.getGraphs().clear();
        collection.getGraphs().addAll(graphs);
    }

    private GraphCollectionSummaryResponse summary(GraphCollection c, int count) {
        return GraphCollectionSummaryResponse.builder().id(c.getId()).name(c.getName()).graphCount(count).build();
    }

    private GraphCollectionResponse response(GraphCollection c, List<Graph> graphs) {
        return GraphCollectionResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .graphs(graphs.stream().map(g -> GraphSummaryResponse.builder()
                        .id(g.getId()).name(g.getName()).image(hasImage(g)).build()).toList())
                .build();
    }

    private boolean hasImage(Graph graph) {
        try {
            return graph.getGraphJson() != null && objectMapper.readTree(graph.getGraphJson()).hasNonNull("image");
        } catch (Exception e) {
            return false;
        }
    }
}
