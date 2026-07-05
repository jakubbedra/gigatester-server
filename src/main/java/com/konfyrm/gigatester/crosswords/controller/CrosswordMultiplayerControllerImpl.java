package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordLetterRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.service.CrosswordMultiplayerService;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class CrosswordMultiplayerControllerImpl implements CrosswordMultiplayerController {

    private final CrosswordMultiplayerService service;

    @Autowired
    public CrosswordMultiplayerControllerImpl(@Nonnull CrosswordMultiplayerService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<?> createSession(CrosswordStateRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.createSession(request, user));
    }

    @Override
    public ResponseEntity<?> joinSession(UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.joinSession(id, user));
    }

    @Override
    public ResponseEntity<?> submitTurn(UUID id, List<CrosswordLetterRequest> letters, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.submitTurn(id, letters, user));
    }

    @Override
    public ResponseEntity<?> getSession(UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getSession(id, user));
    }

    @Override
    public ResponseEntity<?> getMySessions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getMySessions(user));
    }

    @Override
    public ResponseEntity<?> abandonSession(UUID id, @AuthenticationPrincipal User user) {
        service.abandonSession(id, user);
        return ResponseEntity.noContent().build();
    }

}
