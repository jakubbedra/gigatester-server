package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.ExamGradeRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface ExamGradingController {

    @GetMapping("api/v1/tests/{testId}/attempts")
    ResponseEntity<?> getAttempts(@PathVariable("testId") UUID testId, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/states/{stateId}/grading")
    ResponseEntity<?> getGrading(@PathVariable("stateId") UUID stateId, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/states/{stateId}/grading")
    ResponseEntity<?> grade(@PathVariable("stateId") UUID stateId, @RequestBody ExamGradeRequest request,
                            @AuthenticationPrincipal User user);

}
