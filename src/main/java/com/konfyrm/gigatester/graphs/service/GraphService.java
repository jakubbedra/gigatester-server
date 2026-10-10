package com.konfyrm.gigatester.graphs.service;

import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import com.konfyrm.gigatester.graphs.repository.GraphRepository;
import com.konfyrm.gigatester.graphs.repository.GraphSessionRepository;
import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.repository.UserPinRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GraphService {

    private final GraphRepository graphRepository;
    private final GraphSessionRepository graphSessionRepository;
    private final SubjectRepository subjectRepository;
    private final UserPinRepository userPinRepository;

    public GraphService(GraphRepository graphRepository,
                        GraphSessionRepository graphSessionRepository,
                        SubjectRepository subjectRepository,
                        UserPinRepository userPinRepository) {
        this.graphRepository = graphRepository;
        this.graphSessionRepository = graphSessionRepository;
        this.subjectRepository = subjectRepository;
        this.userPinRepository = userPinRepository;
    }

    public Graph addGraph(Graph graph) {
        return graphRepository.save(graph);
    }

    public List<Graph> findGraphs() {
        return graphRepository.findAll();
    }

    public Graph findGraph(UUID id) {
        return graphRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void updateGraph(UUID id, Graph update) {
        Graph existing = findGraph(id);
        existing.setName(update.getName());
        existing.setGraphJson(update.getGraphJson());
        graphRepository.save(existing);
    }

    @Transactional
    public void deleteGraph(UUID id) {
        subjectRepository.findByGraphs_Id(id).forEach(s -> s.getGraphs().removeIf(g -> g.getId().equals(id)));
        graphSessionRepository.deleteByGraph_Id(id);
        userPinRepository.deleteByEntityTypeAndEntityId(PinnedEntityType.GRAPH, id);
        graphRepository.deleteById(id);
    }

}
