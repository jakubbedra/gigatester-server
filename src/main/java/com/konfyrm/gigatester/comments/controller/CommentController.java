package com.konfyrm.gigatester.comments.controller;

import com.konfyrm.gigatester.comments.domain.dto.request.CommentRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface CommentController {

    @PostMapping("api/v1/subjects/{subjectId}/comments")
    ResponseEntity<?> addComment(@PathVariable UUID subjectId,
                                 @RequestBody CommentRequest request,
                                 @AuthenticationPrincipal User user);

    @PostMapping("api/v1/subjects/{subjectId}/comments/{commentId}/replies")
    ResponseEntity<?> addReply(@PathVariable UUID subjectId,
                               @PathVariable UUID commentId,
                               @RequestBody CommentRequest request,
                               @AuthenticationPrincipal User user);

    @PostMapping("api/v1/subjects/{subjectId}/comments/{commentId}/like")
    ResponseEntity<?> like(@PathVariable UUID subjectId,
                           @PathVariable UUID commentId,
                           @AuthenticationPrincipal User user);

    @PostMapping("api/v1/subjects/{subjectId}/comments/{commentId}/dislike")
    ResponseEntity<?> dislike(@PathVariable UUID subjectId,
                              @PathVariable UUID commentId,
                              @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/subjects/{subjectId}/comments/{commentId}")
    ResponseEntity<?> deleteComment(@PathVariable UUID subjectId,
                                    @PathVariable UUID commentId,
                                    @AuthenticationPrincipal User user);
}
