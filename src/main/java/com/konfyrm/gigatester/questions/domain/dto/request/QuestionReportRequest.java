package com.konfyrm.gigatester.questions.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuestionReportRequest {
    private UUID questionId;
    private UUID testId;
    private String message;
    private boolean anonymous;
}
