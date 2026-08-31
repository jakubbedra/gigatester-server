package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.calendar.service.CalendarGroupService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CalendarJoinControllerImpl implements CalendarJoinController {

    private final CalendarGroupService calendarGroupService;

    @Override
    public ResponseEntity<?> preview(String token, User user) {
        return ResponseEntity.ok(calendarGroupService.previewInvite(token, user));
    }

    @Override
    public ResponseEntity<?> join(String token, User user) {
        return ResponseEntity.ok(calendarGroupService.joinByToken(token, user));
    }
}
