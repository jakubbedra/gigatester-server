package com.konfyrm.gigatester.comments.controller;

import com.konfyrm.gigatester.comments.domain.dto.request.CommentRequest;
import com.konfyrm.gigatester.comments.service.CommentService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CommentControllerImpl implements CommentController {

    private final CommentService commentService;

    public CommentControllerImpl(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public ResponseEntity<?> addComment(UUID subjectId, CommentRequest request, User user) {
        return ResponseEntity.ok(commentService.addComment(subjectId, request.getContent(), user));
    }

    @Override
    public ResponseEntity<?> addReply(UUID subjectId, UUID commentId, CommentRequest request, User user) {
        return ResponseEntity.ok(commentService.addReply(subjectId, commentId, request.getContent(), user));
    }

    @Override
    public ResponseEntity<?> like(UUID subjectId, UUID commentId, User user) {
        commentService.like(subjectId, commentId, user);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> dislike(UUID subjectId, UUID commentId, User user) {
        commentService.dislike(subjectId, commentId, user);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> deleteComment(UUID subjectId, UUID commentId, User user) {
        commentService.deleteComment(subjectId, commentId, user);
        return ResponseEntity.noContent().build();
    }
}
