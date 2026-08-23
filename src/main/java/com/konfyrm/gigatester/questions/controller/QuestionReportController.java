package com.konfyrm.gigatester.questions.controller;

import com.konfyrm.gigatester.questions.domain.dto.request.QuestionReportRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/question-reports")
public interface QuestionReportController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> createReport(@RequestBody QuestionReportRequest request, @AuthenticationPrincipal User user);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getReportsForInbox(@AuthenticationPrincipal User user);

    @PutMapping("/{reportId}/resolve")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> resolveReport(@PathVariable UUID reportId, @AuthenticationPrincipal User user);
}
