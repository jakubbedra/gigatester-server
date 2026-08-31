package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarGroupRequest;
import com.konfyrm.gigatester.calendar.domain.dto.request.InviteMemberRequest;
import com.konfyrm.gigatester.calendar.service.CalendarGroupService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CalendarGroupControllerImpl implements CalendarGroupController {

    private final CalendarGroupService calendarGroupService;

    @Override
    public ResponseEntity<?> getVisibleGroups(User user) {
        return ResponseEntity.ok(calendarGroupService.getVisibleGroups(user));
    }

    @Override
    public ResponseEntity<?> createGroup(CalendarGroupRequest request, User user) {
        return ResponseEntity.ok(calendarGroupService.createGroup(request, user));
    }

    @Override
    public ResponseEntity<?> updateGroup(UUID groupId, CalendarGroupRequest request, User user) {
        return ResponseEntity.ok(calendarGroupService.updateGroup(groupId, request, user));
    }

    @Override
    public ResponseEntity<?> deleteGroup(UUID groupId, User user) {
        calendarGroupService.deleteGroup(groupId, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getMembers(UUID groupId, User user) {
        return ResponseEntity.ok(calendarGroupService.getMembers(groupId, user));
    }

    @Override
    public ResponseEntity<?> inviteMember(UUID groupId, InviteMemberRequest request, User user) {
        calendarGroupService.inviteMember(groupId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> removeMember(UUID groupId, UUID userId, User user) {
        calendarGroupService.removeMember(groupId, userId, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getInviteLink(UUID groupId, User user) {
        return ResponseEntity.ok(Map.of("inviteToken", calendarGroupService.getOrCreateInviteToken(groupId, user)));
    }

    @Override
    public ResponseEntity<?> regenerateInviteLink(UUID groupId, User user) {
        return ResponseEntity.ok(Map.of("inviteToken", calendarGroupService.regenerateInviteToken(groupId, user)));
    }
}
