package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CompleteTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.StartTestRunRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/crosswords/{crosswordId}/test-runs")
public interface CrosswordTestRunController {

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> startTestRun(
            @PathVariable UUID crosswordId,
            @RequestBody StartTestRunRequest request,
            @AuthenticationPrincipal User user
    );

    @PostMapping("/complete")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> completeTestRun(
            @PathVariable UUID crosswordId,
            @RequestBody CompleteTestRunRequest request,
            @AuthenticationPrincipal User user
    );

    @GetMapping("/latest")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getLatestRun(
            @PathVariable UUID crosswordId,
            @AuthenticationPrincipal User user
    );
}
