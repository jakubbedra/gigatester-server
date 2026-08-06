package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CompleteTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.StartTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.UpdateTestRunProgressRequest;
import com.konfyrm.gigatester.crosswords.service.CrosswordTestRunService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CrosswordTestRunControllerImpl implements CrosswordTestRunController {

    private final CrosswordTestRunService service;

    @Autowired
    public CrosswordTestRunControllerImpl(CrosswordTestRunService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<?> startTestRun(UUID crosswordId, StartTestRunRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.startTestRun(crosswordId, request, user));
    }

    @Override
    public ResponseEntity<?> updateProgress(UUID crosswordId, UpdateTestRunProgressRequest request, @AuthenticationPrincipal User user) {
        service.updateProgress(crosswordId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> completeTestRun(UUID crosswordId, CompleteTestRunRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.completeTestRun(crosswordId, request, user));
    }

    @Override
    public ResponseEntity<?> getLatestRun(UUID crosswordId, @AuthenticationPrincipal User user) {
        return service.getLatestRun(crosswordId, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> getInProgressRun(UUID crosswordId, @AuthenticationPrincipal User user) {
        return service.getInProgressRun(crosswordId, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
