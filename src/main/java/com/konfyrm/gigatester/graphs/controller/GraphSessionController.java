package com.konfyrm.gigatester.graphs.controller;

import com.konfyrm.gigatester.graphs.domain.dto.request.StartGraphSessionRequest;
import com.konfyrm.gigatester.graphs.domain.dto.request.SubmitGraphAnswerRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface GraphSessionController {

    @PostMapping("api/v1/graph-sessions")
    ResponseEntity<?> startSession(@RequestBody StartGraphSessionRequest request, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/graph-sessions/{id}")
    ResponseEntity<?> getSession(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PostMapping("api/v1/graph-sessions/{id}/answer")
    ResponseEntity<?> submitAnswer(@PathVariable("id") UUID id, @RequestBody SubmitGraphAnswerRequest request, @AuthenticationPrincipal User user);

}
