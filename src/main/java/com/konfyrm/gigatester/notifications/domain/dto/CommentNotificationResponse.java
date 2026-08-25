package com.konfyrm.gigatester.notifications.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CommentNotificationResponse {
    private UUID id;
    private UUID subjectId;
    private String subjectName;
    private UUID commentId;
    private String commenterUsername;
    private String commentPreview;
    private boolean read;
    private LocalDateTime createdAt;
}
