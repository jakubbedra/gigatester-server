package com.konfyrm.gigatester.notifications.controller;

import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface CommentNotificationController {

    @GetMapping("api/v1/notifications")
    ResponseEntity<?> getNotifications(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/notifications/unread-count")
    ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User user);

    @PutMapping("api/v1/notifications/{id}/read")
    ResponseEntity<?> markRead(@PathVariable UUID id, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/notifications/read-all")
    ResponseEntity<?> markAllRead(@AuthenticationPrincipal User user);
}
