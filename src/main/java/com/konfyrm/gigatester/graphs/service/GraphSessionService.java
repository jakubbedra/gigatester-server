package com.konfyrm.gigatester.graphs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphAnswerResponse;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphSessionResponse;
import com.konfyrm.gigatester.graphs.domain.dto.response.GraphVertexView;
import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import com.konfyrm.gigatester.graphs.domain.entity.GraphSession;
import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphDifficulty;
import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphPlayMode;
import com.konfyrm.gigatester.graphs.domain.json.GraphData;
import com.konfyrm.gigatester.graphs.repository.GraphRepository;
import com.konfyrm.gigatester.graphs.repository.GraphSessionRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Drives the "fill in the blanks" game mode: picking which vertex labels a
 * session starts with revealed, and checking the player's guesses against
 * the authored graph's real labels without ever sending those labels to the
 * client ahead of time.
 */
@Service
public class GraphSessionService {

    private final GraphRepository graphRepository;
    private final GraphSessionRepository graphSessionRepository;
    private final ObjectMapper objectMapper;

    private final SubjectGroupAccessService accessService;

    public GraphSessionService(
            GraphRepository graphRepository,
            GraphSessionRepository graphSessionRepository,
            ObjectMapper objectMapper,
            SubjectGroupAccessService accessService
    ) {
        this.graphRepository = graphRepository;
        this.graphSessionRepository = graphSessionRepository;
        this.objectMapper = objectMapper;
        this.accessService = accessService;
    }

    public GraphSessionResponse startSession(UUID graphId, GraphDifficulty difficulty, GraphPlayMode mode, User user) {
        Graph graph = graphRepository.findById(graphId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!accessService.hasAccessToGraph(graphId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        GraphData data = parse(graph);

        List<String> guessableIds = guessableIds(data);
        Set<String> revealed = pickReveal(guessableIds, difficulty);

        GraphSession session = GraphSession.builder()
                .graph(graph)
                .user(user)
                .difficulty(difficulty)
                .mode(mode == null ? GraphPlayMode.TYPING : mode)
                .revealedVertexIds(String.join(",", revealed))
                .solvedVertexIds("")
                .completed(revealed.size() >= guessableIds.size())
                .build();
        session = graphSessionRepository.save(session);

        return toResponse(session, data);
    }

    public GraphSessionResponse getSession(UUID sessionId, User user) {
        GraphSession session = findOwned(sessionId, user);
        return toResponse(session, parse(session.getGraph()));
    }

    public GraphAnswerResponse submitAnswer(UUID sessionId, String vertexId, String guess, User user) {
        GraphSession session = findOwned(sessionId, user);
        GraphData data = parse(session.getGraph());

        GraphData.Vertex vertex = data.vertices.stream()
                .filter(v -> v.id.equals(vertexId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Set<String> revealed = splitIds(session.getRevealedVertexIds());
        Set<String> solved = splitIds(session.getSolvedVertexIds());

        if (revealed.contains(vertexId) || solved.contains(vertexId)) {
            // Already shown — nothing left to guess here, just echo the label back.
            return GraphAnswerResponse.builder().correct(true).vertexId(vertexId).label(vertex.label).completed(session.isCompleted()).build();
        }

        String expected = normalize(vertex.label);
        boolean correct = !expected.isEmpty() && expected.equals(normalize(guess));
        if (!correct) {
            return GraphAnswerResponse.builder().correct(false).vertexId(vertexId).completed(session.isCompleted()).build();
        }

        solved.add(vertexId);
        session.setSolvedVertexIds(String.join(",", solved));
        boolean completed = (revealed.size() + solved.size()) >= guessableIds(data).size();
        session.setCompleted(completed);
        graphSessionRepository.save(session);

        return GraphAnswerResponse.builder().correct(true).vertexId(vertexId).label(vertex.label).completed(completed).build();
    }

    private GraphSession findOwned(UUID sessionId, User user) {
        GraphSession session = graphSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return session;
    }

    private GraphSessionResponse toResponse(GraphSession session, GraphData data) {
        Set<String> revealed = splitIds(session.getRevealedVertexIds());
        Set<String> solved = splitIds(session.getSolvedVertexIds());

        List<GraphVertexView> vertices = data.vertices.stream()
                .map(v -> {
                    boolean guessable = v.label != null && !v.label.isBlank();
                    boolean shown = !guessable || revealed.contains(v.id) || solved.contains(v.id);
                    return GraphVertexView.builder()
                            .id(v.id).x(v.x).y(v.y)
                            .label(shown ? v.label : null)
                            .revealed(shown)
                            .build();
                })
                .toList();

        return GraphSessionResponse.builder()
                .id(session.getId())
                .graphId(session.getGraph().getId())
                .graphName(session.getGraph().getName())
                .difficulty(session.getDifficulty())
                .vertices(vertices)
                .edges(data.edges)
                .hyperedges(data.hyperedges)
                .totalGuessable(guessableIds(data).size())
                .solvedCount(revealed.size() + solved.size())
                .completed(session.isCompleted())
                .showVertices(data.showVertices == null || data.showVertices)
                .edgeStyle("curved".equals(data.edgeStyle) ? "curved" : "straight")
                .mode(session.effectiveMode())
                .bank(session.effectiveMode() == GraphPlayMode.DRAG_DROP ? labelBank(session, data, revealed, solved) : List.of())
                .build();
    }

    /**
     * Labels of the initially-hidden vertices that are still unsolved. The full list is shuffled
     * with a seed derived from the session id, then solved entries are filtered out, so the order
     * is stable across reloads and the bank never reveals which label belongs to which vertex.
     */
    private List<String> labelBank(GraphSession session, GraphData data, Set<String> revealed, Set<String> solved) {
        List<GraphData.Vertex> hidden = data.vertices.stream()
                .filter(v -> v.label != null && !v.label.isBlank() && !revealed.contains(v.id))
                .sorted(Comparator.comparing((GraphData.Vertex v) -> v.id))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(hidden, new Random(session.getId().getMostSignificantBits() ^ session.getId().getLeastSignificantBits()));
        return hidden.stream()
                .filter(v -> !solved.contains(v.id))
                .map(v -> v.label)
                .toList();
    }

    private List<String> guessableIds(GraphData data) {
        return data.vertices.stream()
                .filter(v -> v.label != null && !v.label.isBlank())
                .map(v -> v.id)
                .collect(Collectors.toList());
    }

    private Set<String> pickReveal(List<String> guessableIds, GraphDifficulty difficulty) {
        if (guessableIds.isEmpty()) return Set.of();
        List<String> shuffled = new ArrayList<>(guessableIds);
        Collections.shuffle(shuffled);

        int count;
        if (difficulty == GraphDifficulty.IMPOSSIBLE) {
            count = 1;
        } else {
            count = Math.max(1, (int) Math.round(guessableIds.size() * difficulty.getRevealFraction()));
        }
        // Leave at least one vertex to guess whenever there's more than one — otherwise there's no game.
        count = guessableIds.size() > 1 ? Math.min(count, guessableIds.size() - 1) : guessableIds.size();

        return new HashSet<>(shuffled.subList(0, count));
    }

    private Set<String> splitIds(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(csv.split(",")));
    }

    private String normalize(String s) {
        return AnswerNormalizer.normalize(s);
    }

    private GraphData parse(Graph graph) {
        try {
            GraphData data = objectMapper.readValue(graph.getGraphJson(), GraphData.class);
            if (data.vertices == null) data.vertices = List.of();
            if (data.edges == null) data.edges = List.of();
            if (data.hyperedges == null) data.hyperedges = List.of();
            return data;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Malformed graph data", e);
        }
    }

}
