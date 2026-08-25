package com.konfyrm.gigatester.notifications.controller;

import com.konfyrm.gigatester.notifications.service.CommentNotificationService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentNotificationControllerImpl implements CommentNotificationController {

    private final CommentNotificationService service;

    @Override
    public ResponseEntity<?> getNotifications(User user) {
        return ResponseEntity.ok(service.getNotifications(user));
    }

    @Override
    public ResponseEntity<?> getUnreadCount(User user) {
        return ResponseEntity.ok(Map.of("count", service.getUnreadCount(user)));
    }

    @Override
    public ResponseEntity<?> markRead(UUID id, User user) {
        service.markRead(id, user);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> markAllRead(User user) {
        service.markAllRead(user);
        return ResponseEntity.ok().build();
    }
}
