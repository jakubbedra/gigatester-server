package com.konfyrm.gigatester.comments.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private UUID id;
    private String authorUsername;
    private String authorAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
    private int likes;
    private int dislikes;
    private boolean likedByMe;
    private boolean dislikedByMe;
    private List<CommentResponse> responses;
}
