package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.converter.SubjectGroupConverter;
import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectGroupRequest;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SubjectGroupControllerImpl implements SubjectGroupController {

    private final SubjectGroupService subjectGroupService;
    private final SubjectGroupConverter subjectGroupConverter;
    private final SubjectGroupAccessService accessService;

    public SubjectGroupControllerImpl(SubjectGroupService subjectGroupService,
                                      SubjectGroupConverter subjectGroupConverter,
                                      SubjectGroupAccessService accessService) {
        this.subjectGroupService = subjectGroupService;
        this.subjectGroupConverter = subjectGroupConverter;
        this.accessService = accessService;
    }

    @Override
    public ResponseEntity<?> addSubjectGroup(SubjectGroupRequest request) {
        SubjectGroup entity = subjectGroupConverter.toEntity(request);
        SubjectGroup saved = subjectGroupService.addSubjectGroup(entity);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getSubjectGroups() {
        return ResponseEntity.ok(subjectGroupConverter.toResponse(subjectGroupService.findSubjectGroups()));
    }

    @Override
    public ResponseEntity<?> getSubjectGroup(UUID id) {
        return ResponseEntity.ok(subjectGroupConverter.toResponse(subjectGroupService.findSubjectGroup(id)));
    }

    @Override
    public ResponseEntity<?> updateSubjectGroup(UUID id, SubjectGroupRequest request) {
        SubjectGroup entity = subjectGroupConverter.toEntity(request);
        subjectGroupService.updateSubjectGroup(id, entity);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteSubjectGroup(UUID id) {
        subjectGroupService.deleteSubjectGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> requestAccess(UUID id, @AuthenticationPrincipal User user) {
        accessService.requestAccess(id, user);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getMyAccess(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accessService.getMyAccessStatuses(user.getId()));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAccessRequests(UUID id) {
        return ResponseEntity.ok(accessService.getAccessRequestsForGroup(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRequest(UUID requestId) {
        accessService.approve(requestId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> denyRequest(UUID requestId) {
        accessService.deny(requestId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeAccess(UUID requestId) {
        accessService.revokeAccess(requestId);
        return ResponseEntity.noContent().build();
    }
}
