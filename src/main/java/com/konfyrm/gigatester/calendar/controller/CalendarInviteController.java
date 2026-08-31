package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/calendar/invites")
public interface CalendarInviteController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getMyInvites(@AuthenticationPrincipal User user);

    @PostMapping("/{memberId}/accept")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> acceptInvite(@PathVariable UUID memberId, @AuthenticationPrincipal User user);

    @PostMapping("/{memberId}/decline")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> declineInvite(@PathVariable UUID memberId, @AuthenticationPrincipal User user);

}
