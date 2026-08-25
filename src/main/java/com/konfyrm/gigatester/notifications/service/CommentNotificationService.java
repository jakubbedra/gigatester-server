package com.konfyrm.gigatester.notifications.service;

import com.konfyrm.gigatester.notifications.domain.dto.CommentNotificationResponse;
import com.konfyrm.gigatester.notifications.domain.entity.CommentNotification;
import com.konfyrm.gigatester.notifications.repository.CommentNotificationRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentNotificationService {

    private final CommentNotificationRepository repo;

    private static final int PREVIEW_LENGTH = 150;

    /**
     * Notifies all authors of a subject when someone posts a comment on it.
     * The commenter themselves is excluded from notifications.
     */
    @Transactional
    public void createNotificationsForComment(UUID subjectId, String subjectName, UUID commentId,
                                              String commenterUsername, String content,
                                              User commenter, List<User> subjectAuthors) {
        String preview = content != null && content.length() > PREVIEW_LENGTH
                ? content.substring(0, PREVIEW_LENGTH) + "…"
                : content;
        Set<UUID> notified = new HashSet<>();
        for (User author : subjectAuthors) {
            if (!author.getId().equals(commenter.getId()) && notified.add(author.getId())) {
                repo.save(CommentNotification.builder()
                        .recipient(author)
                        .subjectId(subjectId)
                        .subjectName(subjectName)
                        .commentId(commentId)
                        .commenterUsername(commenterUsername)
                        .commentPreview(preview)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CommentNotificationResponse> getNotifications(User user) {
        return repo.findByRecipient_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return repo.countByRecipient_IdAndReadFalse(user.getId());
    }

    @Transactional
    public void markRead(UUID id, User user) {
        repo.findById(id).ifPresent(n -> {
            if (n.getRecipient().getId().equals(user.getId())) {
                n.setRead(true);
                repo.save(n);
            }
        });
    }

    @Transactional
    public void markAllRead(User user) {
        List<CommentNotification> notifications = repo.findByRecipient_IdOrderByCreatedAtDesc(user.getId());
        notifications.forEach(n -> n.setRead(true));
        repo.saveAll(notifications);
    }

    private CommentNotificationResponse toResponse(CommentNotification n) {
        return CommentNotificationResponse.builder()
                .id(n.getId())
                .subjectId(n.getSubjectId())
                .subjectName(n.getSubjectName())
                .commentId(n.getCommentId())
                .commenterUsername(n.getCommenterUsername())
                .commentPreview(n.getCommentPreview())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
