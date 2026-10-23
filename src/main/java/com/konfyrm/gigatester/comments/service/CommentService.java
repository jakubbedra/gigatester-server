package com.konfyrm.gigatester.comments.service;

import com.konfyrm.gigatester.comments.domain.dto.response.CommentResponse;
import com.konfyrm.gigatester.comments.domain.entity.Comment;
import com.konfyrm.gigatester.comments.repository.CommentRepository;
import com.konfyrm.gigatester.notifications.service.CommentNotificationService;
import com.konfyrm.gigatester.security.service.PermissionService;
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

    /** Longest comment or reply, in characters. The UI shows the same number under the box. */
    public static final int MAX_COMMENT_LENGTH = 250;

    private static String validated(String content) {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment cannot be empty.");
        }
        if (text.length() > MAX_COMMENT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment can have at most " + MAX_COMMENT_LENGTH + " characters.");
        }
        return text;
    }


    private final SubjectRepository subjectRepository;
    private final CommentRepository commentRepository;
    private final PermissionService permissionService;
    private final CommentNotificationService notificationService;

    public CommentService(SubjectRepository subjectRepository, CommentRepository commentRepository,
                          PermissionService permissionService, CommentNotificationService notificationService) {
        this.subjectRepository = subjectRepository;
        this.commentRepository = commentRepository;
        this.permissionService = permissionService;
        this.notificationService = notificationService;
    }

    @Transactional
    public CommentResponse addComment(UUID subjectId, String rawContent, User user) {
        String content = validated(rawContent);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment comment = Comment.builder()
                .user(user)
                .content(content)
                .responses(new java.util.ArrayList<>())
                .build();
        subject.getComments().add(comment);
        subjectRepository.save(subject);
        notificationService.createNotificationsForComment(
                subject.getId(), subject.getName(), comment.getId(),
                user.getUsername(), content, user, subject.getAuthors());
        return toResponse(comment, user.getId());
    }

    @Transactional
    public CommentResponse addReply(UUID subjectId, UUID commentId, String rawContent, User user) {
        String content = validated(rawContent);
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
        if (!isAuthor && !permissionService.isStaff(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        subject.getComments().removeIf(c -> c.getId().equals(commentId));
        subjectRepository.save(subject);
    }

    public CommentResponse toResponse(Comment comment, UUID currentUserId) {
        List<CommentResponse> replies = comment.getResponses() == null ? List.of() :
                comment.getResponses().stream().map(r -> toResponse(r, currentUserId)).toList();
        User author = comment.getUser();
        return CommentResponse.builder()
                .id(comment.getId())
                .authorId(author != null ? author.getId() : null)
                .authorUsername(author != null ? author.getUsername() : "[deleted]")
                .authorAvatarUrl(author != null ? author.getProfilePictureUrl() : null)
                .authorRole(author != null ? author.getRole().name() : null)
                .authorAssignedRoleName(author != null && author.getAssignedRole() != null ? author.getAssignedRole().getName() : null)
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
