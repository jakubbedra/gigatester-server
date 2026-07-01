package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.converter.SubjectConverter;
import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectRequest;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.service.SubjectService;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class SubjectControllerImpl implements SubjectController {

    private final SubjectService subjectService;

    private final SubjectConverter subjectConverter;

    public SubjectControllerImpl(SubjectService subjectService, SubjectConverter subjectConverter) {
        this.subjectService = subjectService;
        this.subjectConverter = subjectConverter;
    }

    @Override
    public ResponseEntity<?> addSubject(SubjectRequest subjectRequest) {
        Subject entity = subjectConverter.toEntity(subjectRequest);
        Subject savedEntity = subjectService.addSubject(entity);
        return ResponseEntity.accepted().body(savedEntity.getId());
    }

    @Override
    public ResponseEntity<?> getSubjects() {
        return ResponseEntity.ok(subjectConverter.toResponse(subjectService.findSubjects()));
    }

    @Override
    public ResponseEntity<?> getSubject(UUID subjectId, @AuthenticationPrincipal User user) {
        UUID userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(subjectConverter.toResponse(subjectService.findSubject(subjectId), userId));
    }

    @Override
    public ResponseEntity<?> updateSubject(UUID subjectId, SubjectRequest subjectRequest) {
        Subject subject = subjectConverter.toEntity(subjectRequest);
        subjectService.updateSubject(subjectId, subject);
        return ResponseEntity.accepted().body(subjectId);
    }

    @Override
    public ResponseEntity<?> deleteSubject(UUID subjectId) {
        subjectService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> addAuthor(UUID subjectId, UUID userId, @AuthenticationPrincipal User user) {
        requireModerator(user);
        Subject subject = subjectService.addAuthor(subjectId, userId);
        return ResponseEntity.ok(subjectConverter.toResponse(subject, user.getId()));
    }

    @Override
    public ResponseEntity<?> removeAuthor(UUID subjectId, UUID userId, @AuthenticationPrincipal User user) {
        requireModerator(user);
        Subject subject = subjectService.removeAuthor(subjectId, userId);
        return ResponseEntity.ok(subjectConverter.toResponse(subject, user.getId()));
    }

    private void requireModerator(User user) {
        if (user == null || (user.getRole() != UserRole.MODERATOR && user.getRole() != UserRole.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}