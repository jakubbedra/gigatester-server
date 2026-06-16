package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.converter.TestConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestControllerImpl implements TestController {

    private final TestService testService;

    private final TestConverter testConverter;

    public TestControllerImpl(TestService testService, TestConverter testConverter) {
        this.testService = testService;
        this.testConverter = testConverter;
    }

    @Override
    public ResponseEntity<?> addTest(TestRequest testRequest) {
        Test entity = testConverter.toEntity(testRequest);
        Test savedEntity = testService.addTest(entity);
        return ResponseEntity.accepted().body(savedEntity.getId());
    }

    @Override
    public ResponseEntity<?> getTests() {
        return ResponseEntity.ok(testConverter.toResponse(testService.findTests()));
    }

    @Override
    public ResponseEntity<?> getTest(UUID testId) {
        return ResponseEntity.ok(testConverter.toResponse(testService.findTest(testId)));
    }

    @Override
    public ResponseEntity<?> updateTest(UUID testId, TestRequest testRequest) {
        Test test = testConverter.toEntity(testRequest);
        testService.updateTest(testId, test);
        return ResponseEntity.accepted().body(test.getId());
    }

    @Override
    public ResponseEntity<?> deleteTest(UUID testId) {
        testService.deleteTest(testId);
        return ResponseEntity.noContent().build();
    }

}
