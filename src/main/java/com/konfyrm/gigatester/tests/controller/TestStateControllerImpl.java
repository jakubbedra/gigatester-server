package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.converter.TestStateConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.TestStateUpdateRequest;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.tests.service.TestStateService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
public class TestStateControllerImpl implements TestStateController {

    private final TestStateService testStateService;
    private final TestStateConverter testStateConverter;

    @Autowired
    public TestStateControllerImpl(
            TestStateService testStateService,
            TestStateConverter testStateConverter
    ) {
        this.testStateService = testStateService;
        this.testStateConverter = testStateConverter;
    }

    @Override
    public ResponseEntity<?> createTestState(UUID testId, TestStateRequest request,
                                             @AuthenticationPrincipal User user) {
        TestState testState = testStateService.createTestState(testId, request, user);
        return ResponseEntity.accepted().body(testState.getId());
    }

    @Override
    public ResponseEntity<?> getTestStateFromTestId(UUID testId, @AuthenticationPrincipal User user) {
        Optional<TestState> stateOpt = testStateService.findTestStateByTestAndUser(testId, user.getId());
        if (stateOpt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(testStateConverter.toResponse(stateOpt.get()));
    }

    @Override
    public ResponseEntity<?> getTestState(UUID testStateId, @AuthenticationPrincipal User user) {
        TestState testState = testStateService.findTestState(testStateId, user.getId());
        return ResponseEntity.ok(testStateConverter.toResponse(testState));
    }

    @Override
    public ResponseEntity<?> updateTestState(UUID testStateId, TestStateUpdateRequest request,
                                             @AuthenticationPrincipal User user) {
        testStateService.findTestState(testStateId, user.getId()); // ownership check
        testStateService.updateTestExecutionState(testStateId, request.getAction());
        return ResponseEntity.accepted().body(testStateId);
    }
}
