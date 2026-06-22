package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface TestStateController {

    //todo: remember about deleting question states!!!!
    @PostMapping("api/v1/tests/{testId}/states")
    ResponseEntity<?> createTestState(@PathVariable("testId") UUID testId, @RequestBody TestStateRequest request);

    @GetMapping("api/v1/tests/{testId}/states")
    ResponseEntity<?> getTestStateFromTestId(@PathVariable("testId") UUID testId);

    @GetMapping("api/v1/states/{testStateId}")
    ResponseEntity<?> getTestState(@PathVariable("testStateId") UUID testStateId);

    // todo: get test execution by test Id (and user id in the future when users will be supported, that is before making it public)
    @PutMapping("api/v1/states/{testStateId}")
    ResponseEntity<?> updateTestState(@PathVariable("testStateId") UUID testStateId, @RequestBody TestStateUpdateRequest request);

}
