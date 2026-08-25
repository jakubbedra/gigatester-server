package com.konfyrm.gigatester.questions.service;

import com.konfyrm.gigatester.questions.domain.dto.request.QuestionReportRequest;
import com.konfyrm.gigatester.questions.domain.dto.response.QuestionReportResponse;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.domain.entity.QuestionReport;
import com.konfyrm.gigatester.questions.repository.QuestionReportRepository;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionReportService {

    private static final int PREVIEW_LENGTH = 120;

    private final QuestionReportRepository questionReportRepository;
    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final PermissionService permissionService;

    public QuestionReportService(
            QuestionReportRepository questionReportRepository,
            QuestionRepository questionRepository,
            TestRepository testRepository,
            PermissionService permissionService
    ) {
        this.questionReportRepository = questionReportRepository;
        this.questionRepository = questionRepository;
        this.testRepository = testRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public void createReport(QuestionReportRequest request, User user) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found"));

        QuestionReport report = QuestionReport.builder()
                .question(question)
                .test(test)
                .reportedBy(request.isAnonymous() ? null : user)
                .anonymous(request.isAnonymous())
                .message(request.getMessage())
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();
        questionReportRepository.save(report);
    }

    /** Admins see all reports (including resolved); everyone else sees all reports for tests they author. */
    public List<QuestionReportResponse> getReportsForInbox(User user) {
        List<QuestionReport> reports = permissionService.isAdmin(user)
                ? questionReportRepository.findAllByOrderByCreatedAtDesc()
                : questionReportRepository.findByTest_IdInOrderByCreatedAtDesc(
                        testRepository.findByAuthors_Id(user.getId()).stream().map(Test::getId).toList());
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void resolveReport(UUID reportId, User user) {
        QuestionReport report = questionReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        boolean isAuthor = report.getTest().getAuthors().stream().anyMatch(a -> a.getId().equals(user.getId()));
        if (!permissionService.isAdmin(user) && !isAuthor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        report.setResolved(true);
        questionReportRepository.save(report);
    }

    private QuestionReportResponse toResponse(QuestionReport report) {
        String text = report.getQuestion().getContent() != null ? report.getQuestion().getContent().getText() : null;
        String preview = text == null ? "" : text.length() > PREVIEW_LENGTH ? text.substring(0, PREVIEW_LENGTH) + "…" : text;
        return QuestionReportResponse.builder()
                .id(report.getId())
                .questionId(report.getQuestion().getId())
                .questionPreview(preview)
                .testId(report.getTest().getId())
                .testName(report.getTest().getName())
                .reporterUsername(report.isAnonymous() || report.getReportedBy() == null ? null : report.getReportedBy().getUsername())
                .anonymous(report.isAnonymous())
                .message(report.getMessage())
                .createdAt(report.getCreatedAt())
                .resolved(report.isResolved())
                .build();
    }
}
