package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarEventRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/calendar/events")
public interface CalendarEventController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getEvents(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) List<UUID> groupIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    );

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> createEvent(@RequestBody CalendarEventRequest request, @AuthenticationPrincipal User user);

    @PutMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> updateEvent(@PathVariable UUID eventId, @RequestBody CalendarEventRequest request, @AuthenticationPrincipal User user);

    @DeleteMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> deleteEvent(@PathVariable UUID eventId, @AuthenticationPrincipal User user);

}
