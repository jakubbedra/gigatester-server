package com.konfyrm.gigatester.coding.controller;

import com.konfyrm.gigatester.coding.domain.dto.request.CodingTaskRequest;
import com.konfyrm.gigatester.coding.domain.dto.request.SubmitCodingRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface CodingTaskController {

    @PostMapping("api/v1/coding-tasks")
    ResponseEntity<?> addTask(@RequestBody CodingTaskRequest request, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/coding-tasks")
    ResponseEntity<?> getTasks(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/coding-tasks/playable")
    ResponseEntity<?> getPlayableTasks(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/coding-tasks/{id}")
    ResponseEntity<?> getTask(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/coding-tasks/{id}")
    ResponseEntity<?> updateTask(@PathVariable("id") UUID id, @RequestBody CodingTaskRequest request,
                                 @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/coding-tasks/{id}")
    ResponseEntity<?> deleteTask(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/coding-tasks/{id}/play")
    ResponseEntity<?> play(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PostMapping("api/v1/coding-tasks/{id}/problems/{problemId}/submissions")
    ResponseEntity<?> submit(@PathVariable("id") UUID id, @PathVariable("problemId") UUID problemId,
                             @RequestBody SubmitCodingRequest request,
                             @AuthenticationPrincipal User user);

    @GetMapping("api/v1/coding-tasks/{id}/results")
    ResponseEntity<?> getResults(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/coding-submissions/{submissionId}")
    ResponseEntity<?> getSubmission(@PathVariable("submissionId") UUID submissionId, @AuthenticationPrincipal User user);

}
