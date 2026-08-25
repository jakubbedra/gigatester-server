package com.konfyrm.gigatester.questions.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class QuestionReportResponse {
    private UUID id;
    private UUID questionId;
    private String questionPreview;
    private UUID testId;
    private String testName;
    /** Null when submitted anonymously. */
    private String reporterUsername;
    private boolean anonymous;
    private String message;
    private LocalDateTime createdAt;
    private boolean resolved;
}
