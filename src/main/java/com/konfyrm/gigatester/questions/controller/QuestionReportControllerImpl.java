package com.konfyrm.gigatester.questions.controller;

import com.konfyrm.gigatester.questions.domain.dto.request.QuestionReportRequest;
import com.konfyrm.gigatester.questions.service.QuestionReportService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class QuestionReportControllerImpl implements QuestionReportController {

    private final QuestionReportService questionReportService;

    @Override
    public ResponseEntity<?> createReport(QuestionReportRequest request, User user) {
        questionReportService.createReport(request, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getReportsForInbox(User user) {
        return ResponseEntity.ok(questionReportService.getReportsForInbox(user));
    }

    @Override
    public ResponseEntity<?> resolveReport(UUID reportId, User user) {
        questionReportService.resolveReport(reportId, user);
        return ResponseEntity.noContent().build();
    }
}
