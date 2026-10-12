package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.dto.request.ExamGradeRequest;
import com.konfyrm.gigatester.tests.service.ExamGradingService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ExamGradingControllerImpl implements ExamGradingController {

    private final ExamGradingService examGradingService;

    public ExamGradingControllerImpl(ExamGradingService examGradingService) {
        this.examGradingService = examGradingService;
    }

    @Override
    public ResponseEntity<?> getAttempts(UUID testId, User user) {
        return ResponseEntity.ok(examGradingService.listAttempts(testId, user));
    }

    @Override
    public ResponseEntity<?> getGrading(UUID stateId, User user) {
        return ResponseEntity.ok(examGradingService.getGrading(stateId, user));
    }

    @Override
    public ResponseEntity<?> grade(UUID stateId, ExamGradeRequest request, User user) {
        examGradingService.grade(stateId, request, user);
        return ResponseEntity.noContent().build();
    }

}
