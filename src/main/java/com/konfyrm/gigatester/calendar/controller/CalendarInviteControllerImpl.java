package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.calendar.service.CalendarGroupService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CalendarInviteControllerImpl implements CalendarInviteController {

    private final CalendarGroupService calendarGroupService;

    @Override
    public ResponseEntity<?> getMyInvites(User user) {
        return ResponseEntity.ok(calendarGroupService.getMyInvites(user));
    }

    @Override
    public ResponseEntity<?> acceptInvite(UUID memberId, User user) {
        calendarGroupService.acceptInvite(memberId, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> declineInvite(UUID memberId, User user) {
        calendarGroupService.declineInvite(memberId, user);
        return ResponseEntity.noContent().build();
    }
}
