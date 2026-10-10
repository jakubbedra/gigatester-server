package com.konfyrm.gigatester.graphs.controller;

import com.konfyrm.gigatester.graphs.domain.dto.request.StartGraphSessionRequest;
import com.konfyrm.gigatester.graphs.domain.dto.request.SubmitGraphAnswerRequest;
import com.konfyrm.gigatester.graphs.service.GraphSessionService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GraphSessionControllerImpl implements GraphSessionController {

    private final GraphSessionService graphSessionService;

    public GraphSessionControllerImpl(GraphSessionService graphSessionService) {
        this.graphSessionService = graphSessionService;
    }

    @Override
    public ResponseEntity<?> startSession(StartGraphSessionRequest request, User user) {
        return ResponseEntity.ok(graphSessionService.startSession(request.getGraphId(), request.getDifficulty(), request.getMode(), user));
    }

    @Override
    public ResponseEntity<?> getSession(UUID id, User user) {
        return ResponseEntity.ok(graphSessionService.getSession(id, user));
    }

    @Override
    public ResponseEntity<?> submitAnswer(UUID id, SubmitGraphAnswerRequest request, User user) {
        return ResponseEntity.ok(graphSessionService.submitAnswer(id, request.getVertexId(), request.getGuess(), user));
    }

}
