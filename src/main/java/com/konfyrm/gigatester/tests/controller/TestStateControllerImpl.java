package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.converter.TestStateConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateUpdateRequest;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.tests.service.TestStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestStateControllerImpl implements TestStateController {

    private final TestStateService testStateService;
    private final TestStateConverter testStateConverter;

    public TestStateControllerImpl(
            TestStateService testStateService,
            TestStateConverter testStateConverter
    ) {
        this.testStateService = testStateService;
        this.testStateConverter = testStateConverter;
    }

    @Override
    public ResponseEntity<?> createTestState(UUID testId, TestStateRequest request) {
        // todo: change
        TestState testState = testStateService.createTestState(testId, request);
        return ResponseEntity.accepted().body(testState.getId());
    }

    @Override
    public ResponseEntity<?> getTestStateFromTestId(UUID testId) {
        TestState testState = testStateService.findTestStateByTestId(testId);
        return ResponseEntity.ok(testStateConverter.toResponse(testState));
    }

    @Override
    public ResponseEntity<?> getTestState(UUID testStateId) {
        TestState testState = testStateService.findTestState(testStateId);
        return ResponseEntity.ok(testStateConverter.toResponse(testState));
    }

    @Override
    public ResponseEntity<?> updateTestState(UUID testStateId, TestStateUpdateRequest request) {
        testStateService.updateTestExecutionState(testStateId, request.getAction());
        return ResponseEntity.accepted().body(testStateId);
    }

}