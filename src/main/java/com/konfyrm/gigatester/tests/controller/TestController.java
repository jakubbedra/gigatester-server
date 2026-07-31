package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


public interface TestController {

    @PostMapping("api/v1/tests")
    ResponseEntity<?> addTest(@RequestBody TestRequest testRequest, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/tests")
    ResponseEntity<?> getTests();

    @GetMapping("api/v1/tests/{testId}")
    ResponseEntity<?> getTest(@PathVariable("testId") UUID testId, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/tests/{testId}")
    ResponseEntity<?> updateTest(@PathVariable("testId") UUID testId, @RequestBody TestRequest testRequest, @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/tests/{testId}")
    ResponseEntity<?> deleteTest(@PathVariable("testId") UUID testId, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/tests/{testId}/question-counts")
    ResponseEntity<?> getQuestionCounts(
            @PathVariable("testId") UUID testId,
            @RequestParam(value = "tagIds", required = false) List<UUID> tagIds,
            @RequestParam(value = "excludeTags", defaultValue = "false") boolean excludeTags,
            @AuthenticationPrincipal User user
    );

    @PutMapping("api/v1/tests/{testId}/authors/{userId}")
    ResponseEntity<?> addAuthor(@PathVariable("testId") UUID testId, @PathVariable("userId") UUID userId, @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/tests/{testId}/authors/{userId}")
    ResponseEntity<?> removeAuthor(@PathVariable("testId") UUID testId, @PathVariable("userId") UUID userId, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/tests/{testId}/tags")
    ResponseEntity<?> getTestTags(@PathVariable("testId") UUID testId, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/tests/{testId}/questions")
    ResponseEntity<?> getTestQuestions(
            @PathVariable("testId") UUID testId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @AuthenticationPrincipal User user
    );

    @GetMapping("api/v1/tests/{testId}/author-candidates")
    ResponseEntity<?> getAuthorCandidates(@PathVariable("testId") UUID testId, @AuthenticationPrincipal User user);

}