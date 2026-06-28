package com.konfyrm.gigatester.comments.service;

import com.konfyrm.gigatester.comments.domain.dto.response.CommentResponse;
import com.konfyrm.gigatester.comments.domain.entity.Comment;
import com.konfyrm.gigatester.comments.repository.CommentRepository;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final SubjectRepository subjectRepository;
    private final CommentRepository commentRepository;

    public CommentService(SubjectRepository subjectRepository, CommentRepository commentRepository) {
        this.subjectRepository = subjectRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public CommentResponse addComment(UUID subjectId, String content, User user) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment comment = Comment.builder()
                .user(user)
                .content(content)
                .responses(new java.util.ArrayList<>())
                .build();
        subject.getComments().add(comment);
        subjectRepository.save(subject);
        return toResponse(comment, user.getId());
    }

    @Transactional
    public CommentResponse addReply(UUID subjectId, UUID commentId, String content, User user) {
        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        Comment reply = Comment.builder()
                .user(user)
                .content(content)
                .responses(new java.util.ArrayList<>())
                .build();
        parent.getResponses().add(reply);
        commentRepository.save(parent);
        return toResponse(reply, user.getId());
    }

    @Transactional
    public void like(UUID subjectId, UUID commentId, User user) {
        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        UUID userId = user.getId();
        if (comment.getLikedBy().contains(userId)) {
            comment.getLikedBy().remove(userId);
        } else {
            comment.getLikedBy().add(userId);
            comment.getDislikedBy().remove(userId);
        }
        commentRepository.save(comment);
    }

    @Transactional
    public void dislike(UUID subjectId, UUID commentId, User user) {
        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        UUID userId = user.getId();
        if (comment.getDislikedBy().contains(userId)) {
            comment.getDislikedBy().remove(userId);
        } else {
            comment.getDislikedBy().add(userId);
            comment.getLikedBy().remove(userId);
        }
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UUID subjectId, UUID commentId, User user) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isPrivileged = user.getRole() == com.konfyrm.gigatester.users.domain.entity.UserRole.ADMIN
                || user.getRole() == com.konfyrm.gigatester.users.domain.entity.UserRole.MODERATOR;
        if (!isAuthor && !isPrivileged) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        subject.getComments().removeIf(c -> c.getId().equals(commentId));
        subjectRepository.save(subject);
    }

    public CommentResponse toResponse(Comment comment, UUID currentUserId) {
        List<CommentResponse> replies = comment.getResponses() == null ? List.of() :
                comment.getResponses().stream().map(r -> toResponse(r, currentUserId)).toList();
        return CommentResponse.builder()
                .id(comment.getId())
                .authorUsername(comment.getUser().getUsername())
                .authorAvatarUrl(comment.getUser().getProfilePictureUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likes(comment.getLikedBy().size())
                .dislikes(comment.getDislikedBy().size())
                .likedByMe(currentUserId != null && comment.getLikedBy().contains(currentUserId))
                .dislikedByMe(currentUserId != null && comment.getDislikedBy().contains(currentUserId))
                .responses(replies)
                .build();
    }
}
