package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Redemption endpoints for a calendar group's QR/link invite (see /calendar/join/{token} in the UI). */
@RequestMapping("/api/v1/calendar/join")
public interface CalendarJoinController {

    @GetMapping("/{token}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> preview(@PathVariable String token, @AuthenticationPrincipal User user);

    @PostMapping("/{token}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> join(@PathVariable String token, @AuthenticationPrincipal User user);

}
