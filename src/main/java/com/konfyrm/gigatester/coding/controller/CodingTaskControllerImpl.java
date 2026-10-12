package com.konfyrm.gigatester.coding.controller;

import com.konfyrm.gigatester.coding.domain.dto.request.CodingTaskRequest;
import com.konfyrm.gigatester.coding.domain.dto.request.SubmitCodingRequest;
import com.konfyrm.gigatester.coding.domain.entity.CodingTask;
import com.konfyrm.gigatester.coding.service.CodingSubmissionService;
import com.konfyrm.gigatester.coding.service.CodingTaskService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CodingTaskControllerImpl implements CodingTaskController {

    private final CodingTaskService taskService;
    private final CodingSubmissionService submissionService;
    private final PermissionService permissionService;
    private final SubjectGroupAccessService accessService;

    public CodingTaskControllerImpl(CodingTaskService taskService, CodingSubmissionService submissionService,
                                    PermissionService permissionService, SubjectGroupAccessService accessService) {
        this.taskService = taskService;
        this.submissionService = submissionService;
        this.permissionService = permissionService;
        this.accessService = accessService;
    }

    @Override
    public ResponseEntity<?> addTask(CodingTaskRequest request, User user) {
        permissionService.require(permissionService.canCreate(user, Permission.CODING_WRITE));
        CodingTask saved = taskService.add(request, user);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getTasks(User user) {
        permissionService.require(permissionService.isStaff(user));
        return ResponseEntity.ok(taskService.findAll().stream().map(taskService::toSummary).toList());
    }

    @Override
    public ResponseEntity<?> getPlayableTasks(User user) {
        return ResponseEntity.ok(taskService.findAll().stream()
                .filter(t -> accessService.hasAccessToCodingTask(t.getId(), user))
                .map(taskService::toSummary)
                .toList());
    }

    @Override
    public ResponseEntity<?> getTask(UUID id, User user) {
        permissionService.require(permissionService.hasCodingTaskPermission(user, id, Permission.CODING_WRITE));
        return ResponseEntity.ok(taskService.toResponse(taskService.find(id)));
    }

    @Override
    public ResponseEntity<?> updateTask(UUID id, CodingTaskRequest request, User user) {
        permissionService.require(permissionService.hasCodingTaskPermission(user, id, Permission.CODING_WRITE));
        taskService.update(id, request);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteTask(UUID id, User user) {
        permissionService.require(permissionService.hasCodingTaskPermission(user, id, Permission.CODING_WRITE));
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> play(UUID id, User user) {
        return ResponseEntity.ok(submissionService.play(id, user));
    }

    @Override
    public ResponseEntity<?> submit(UUID id, UUID problemId, SubmitCodingRequest request, User user) {
        return ResponseEntity.ok(submissionService.submit(id, problemId, request, user));
    }

    @Override
    public ResponseEntity<?> getResults(UUID id, User user) {
        return ResponseEntity.ok(submissionService.results(id, user));
    }

    @Override
    public ResponseEntity<?> getSubmission(UUID submissionId, User user) {
        return ResponseEntity.ok(submissionService.detail(submissionId, user));
    }

}
