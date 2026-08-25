package com.konfyrm.gigatester.notifications.repository;

import com.konfyrm.gigatester.notifications.domain.entity.CommentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentNotificationRepository extends JpaRepository<CommentNotification, UUID> {

    List<CommentNotification> findByRecipient_IdOrderByCreatedAtDesc(UUID recipientId);

    long countByRecipient_IdAndReadFalse(UUID recipientId);
}
