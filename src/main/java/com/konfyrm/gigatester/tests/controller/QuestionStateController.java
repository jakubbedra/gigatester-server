package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface QuestionStateController {

    @GetMapping("api/v1/states/{testStateId}/answers/{questionStateId}")
    ResponseEntity<?> getQuestionState(
            @PathVariable("testStateId") UUID testStateId,
            @PathVariable("questionStateId") UUID questionStateId,
            @AuthenticationPrincipal User user
    );

    @PutMapping("api/v1/states/{testStateId}/answers/{questionStateId}")
    ResponseEntity<?> updateQuestionState(
            @PathVariable("testStateId") UUID testStateId,
            @PathVariable("questionStateId") UUID questionStateId,
            @RequestBody QuestionStateRequest request,
            @AuthenticationPrincipal User user
    );

}
