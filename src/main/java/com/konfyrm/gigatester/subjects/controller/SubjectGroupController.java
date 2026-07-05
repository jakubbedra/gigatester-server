package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectGroupRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface SubjectGroupController {

    @PostMapping("api/v1/subject-groups")
    ResponseEntity<?> addSubjectGroup(@RequestBody SubjectGroupRequest request);

    @GetMapping("api/v1/subject-groups")
    ResponseEntity<?> getSubjectGroups();

    @GetMapping("api/v1/subject-groups/{id}")
    ResponseEntity<?> getSubjectGroup(@PathVariable("id") UUID id);

    @PutMapping("api/v1/subject-groups/{id}")
    ResponseEntity<?> updateSubjectGroup(@PathVariable("id") UUID id, @RequestBody SubjectGroupRequest request);

    @DeleteMapping("api/v1/subject-groups/{id}")
    ResponseEntity<?> deleteSubjectGroup(@PathVariable("id") UUID id);

    @PostMapping("api/v1/subject-groups/{id}/request-access")
    ResponseEntity<?> requestAccess(@PathVariable("id") UUID id,
                                    @AuthenticationPrincipal User user);

    @GetMapping("api/v1/subject-groups/my-access")
    ResponseEntity<?> getMyAccess(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/subject-groups/{id}/access-requests")
    ResponseEntity<?> getAccessRequests(@PathVariable("id") UUID id,
                                        @AuthenticationPrincipal User user);

    @PutMapping("api/v1/subject-groups/access-requests/{requestId}/approve")
    ResponseEntity<?> approveRequest(@PathVariable("requestId") UUID requestId,
                                     @AuthenticationPrincipal User user);

    @PutMapping("api/v1/subject-groups/access-requests/{requestId}/deny")
    ResponseEntity<?> denyRequest(@PathVariable("requestId") UUID requestId,
                                  @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/subject-groups/access-requests/{requestId}")
    ResponseEntity<?> revokeAccess(@PathVariable("requestId") UUID requestId,
                                   @AuthenticationPrincipal User user);

    @PutMapping("api/v1/subject-groups/{id}/owners/{userId}")
    ResponseEntity<?> addOwner(@PathVariable("id") UUID id,
                               @PathVariable("userId") UUID userId);

    @DeleteMapping("api/v1/subject-groups/{id}/owners/{userId}")
    ResponseEntity<?> removeOwner(@PathVariable("id") UUID id,
                                  @PathVariable("userId") UUID userId);

    @GetMapping("api/v1/subject-groups/{id}/owner-candidates")
    ResponseEntity<?> getOwnerCandidates(@PathVariable("id") UUID id);

}
