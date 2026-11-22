package com.konfyrm.gigatester.graphs.controller;

import com.konfyrm.gigatester.graphs.domain.dto.request.GraphCollectionRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("api/v1/graph-collections")
public interface GraphCollectionController {

    /** Staff overview. */
    @GetMapping
    ResponseEntity<?> list(@AuthenticationPrincipal User user);

    /** Collections a player can start (at least one graph they may open). */
    @GetMapping("/playable")
    ResponseEntity<?> playable(@AuthenticationPrincipal User user);

    @PostMapping
    ResponseEntity<?> create(@RequestBody GraphCollectionRequest request, @AuthenticationPrincipal User user);

    /** Editor view (staff). */
    @GetMapping("/{id}")
    ResponseEntity<?> get(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    /** Play view: only the graphs this player may open. */
    @GetMapping("/{id}/play")
    ResponseEntity<?> play(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PutMapping("/{id}")
    ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody GraphCollectionRequest request, @AuthenticationPrincipal User user);

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);
}
