package com.konfyrm.gigatester.calendar.controller;

import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarGroupRequest;
import com.konfyrm.gigatester.calendar.domain.dto.request.InviteMemberRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/calendar/groups")
public interface CalendarGroupController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getVisibleGroups(@AuthenticationPrincipal User user);

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> createGroup(@RequestBody CalendarGroupRequest request, @AuthenticationPrincipal User user);

    @PutMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> updateGroup(@PathVariable UUID groupId, @RequestBody CalendarGroupRequest request, @AuthenticationPrincipal User user);

    @DeleteMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> deleteGroup(@PathVariable UUID groupId, @AuthenticationPrincipal User user);

    @GetMapping("/{groupId}/members")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getMembers(@PathVariable UUID groupId, @AuthenticationPrincipal User user);

    @PostMapping("/{groupId}/members")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> inviteMember(@PathVariable UUID groupId, @RequestBody InviteMemberRequest request, @AuthenticationPrincipal User user);

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId, @AuthenticationPrincipal User user);

    @PostMapping("/{groupId}/invite-link")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> getInviteLink(@PathVariable UUID groupId, @AuthenticationPrincipal User user);

    @PostMapping("/{groupId}/invite-link/regenerate")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<?> regenerateInviteLink(@PathVariable UUID groupId, @AuthenticationPrincipal User user);

}
