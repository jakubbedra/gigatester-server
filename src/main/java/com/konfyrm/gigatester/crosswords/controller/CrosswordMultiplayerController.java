package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordLetterRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/crossword-multiplayer-sessions")
public interface CrosswordMultiplayerController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> createSession(
            @RequestBody CrosswordStateRequest request,
            @AuthenticationPrincipal User user
    );

    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> joinSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    );

    @PutMapping("/{id}/turn")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> submitTurn(
            @PathVariable UUID id,
            @RequestBody List<CrosswordLetterRequest> letters,
            @AuthenticationPrincipal User user
    );

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    );

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getMySessions(@AuthenticationPrincipal User user);

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> abandonSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    );

}
