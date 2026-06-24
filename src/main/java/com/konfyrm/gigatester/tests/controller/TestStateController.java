package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateUpdateRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface TestStateController {

    @PostMapping("api/v1/tests/{testId}/states")
    ResponseEntity<?> createTestState(@PathVariable("testId") UUID testId,
                                      @RequestBody TestStateRequest request,
                                      @AuthenticationPrincipal User user);

    @GetMapping("api/v1/tests/{testId}/states")
    ResponseEntity<?> getTestStateFromTestId(@PathVariable("testId") UUID testId,
                                             @AuthenticationPrincipal User user);

    @GetMapping("api/v1/states/{testStateId}")
    ResponseEntity<?> getTestState(@PathVariable("testStateId") UUID testStateId,
                                   @AuthenticationPrincipal User user);

    @PutMapping("api/v1/states/{testStateId}")
    ResponseEntity<?> updateTestState(@PathVariable("testStateId") UUID testStateId,
                                      @RequestBody TestStateUpdateRequest request,
                                      @AuthenticationPrincipal User user);

}
