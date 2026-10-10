package com.konfyrm.gigatester.graphs.controller;

import com.konfyrm.gigatester.graphs.domain.dto.request.GraphRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface GraphController {

    @PostMapping("api/v1/graphs")
    ResponseEntity<?> addGraph(@RequestBody GraphRequest request, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/graphs")
    ResponseEntity<?> getGraphs(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/graphs/playable")
    ResponseEntity<?> getPlayableGraphs(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/graphs/{id}")
    ResponseEntity<?> getGraph(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/graphs/{id}")
    ResponseEntity<?> updateGraph(@PathVariable("id") UUID id, @RequestBody GraphRequest request, @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/graphs/{id}")
    ResponseEntity<?> deleteGraph(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

}
