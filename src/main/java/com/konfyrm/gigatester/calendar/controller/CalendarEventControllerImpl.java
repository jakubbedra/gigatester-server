package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarEventRequest;
import com.konfyrm.gigatester.calendar.service.CalendarEventService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CalendarEventControllerImpl implements CalendarEventController {

    private final CalendarEventService calendarEventService;

    @Override
    public ResponseEntity<?> getEvents(User user, List<UUID> groupIds, LocalDateTime from, LocalDateTime to) {
        return ResponseEntity.ok(calendarEventService.getEvents(user, groupIds, from, to));
    }

    @Override
    public ResponseEntity<?> createEvent(CalendarEventRequest request, User user) {
        return ResponseEntity.ok(calendarEventService.createEvent(request, user));
    }

    @Override
    public ResponseEntity<?> updateEvent(UUID eventId, CalendarEventRequest request, User user) {
        return ResponseEntity.ok(calendarEventService.updateEvent(eventId, request, user));
    }

    @Override
    public ResponseEntity<?> deleteEvent(UUID eventId, User user) {
        calendarEventService.deleteEvent(eventId, user);
        return ResponseEntity.noContent().build();
    }
}
