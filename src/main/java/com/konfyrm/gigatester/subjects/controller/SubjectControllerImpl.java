package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.converter.SubjectConverter;
import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectRequest;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupAccessRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.subjects.service.SubjectService;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SubjectControllerImpl implements SubjectController {

    private final SubjectService subjectService;
    private final SubjectConverter subjectConverter;
    private final SubjectGroupRepository subjectGroupRepository;
    private final SubjectGroupAccessRepository subjectGroupAccessRepository;
    private final UserRepository userRepository;

    public SubjectControllerImpl(SubjectService subjectService, SubjectConverter subjectConverter,
                                 SubjectGroupRepository subjectGroupRepository,
                                 SubjectGroupAccessRepository subjectGroupAccessRepository,
                                 UserRepository userRepository) {
        this.subjectService = subjectService;
        this.subjectConverter = subjectConverter;
        this.subjectGroupRepository = subjectGroupRepository;
        this.subjectGroupAccessRepository = subjectGroupAccessRepository;
        this.userRepository = userRepository;
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

    @Override
    public ResponseEntity<?> getAuthorCandidates(UUID subjectId, @AuthenticationPrincipal User user) {
        requireModerator(user);
        Subject subject = subjectService.findSubject(subjectId);
        Set<UUID> alreadyAuthors = subject.getAuthors().stream()
                .map(User::getId).collect(Collectors.toSet());

        // Collect approved members from all groups that contain this subject
        Set<UUID> candidateIds = new LinkedHashSet<>();
        subjectGroupRepository.findByTests_Id(subjectId).forEach(group ->
            subjectGroupAccessRepository
                .findBySubjectGroup_IdAndStatus(group.getId(), SubjectGroupAccessStatus.APPROVED)
                .forEach(req -> candidateIds.add(req.getUser().getId()))
        );

        // Also include all moderators and admins
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.MODERATOR || u.getRole() == UserRole.ADMIN)
                .map(User::getId)
                .forEach(candidateIds::add);

        List<UserResponse> candidates = candidateIds.stream()
                .filter(id -> !alreadyAuthors.contains(id))
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .role(u.getRole())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .build())
                .sorted(Comparator.comparing(UserResponse::getUsername))
                .collect(Collectors.toList());

        return ResponseEntity.ok(candidates);
    }

    private void requireModerator(User user) {
        if (user == null || (user.getRole() != UserRole.MODERATOR && user.getRole() != UserRole.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}