package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface TestController {

    @PostMapping("api/v1/tests")
    ResponseEntity<?> addTest(@RequestBody TestRequest testRequest);

    @GetMapping("api/v1/tests")
    ResponseEntity<?> getTests();

    @GetMapping("api/v1/tests/{testId}")
    ResponseEntity<?> getTest(@PathVariable("testId") UUID testId);

    @PutMapping("api/v1/tests/{testId}")
    ResponseEntity<?> updateTest(@PathVariable("testId") UUID testId, @RequestBody TestRequest testRequest);

    @DeleteMapping("api/v1/tests/{testId}")
    ResponseEntity<?> deleteTest(@PathVariable("testId") UUID testId);

    @GetMapping("api/v1/tests/{testId}/question-counts")
    ResponseEntity<?> getQuestionCounts(
            @PathVariable("testId") UUID testId,
            @RequestParam(value = "tagIds", required = false) List<UUID> tagIds
    );

}