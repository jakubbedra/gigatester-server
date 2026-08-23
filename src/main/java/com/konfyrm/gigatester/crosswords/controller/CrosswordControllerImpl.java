package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.config.CrosswordProperties;
import com.konfyrm.gigatester.crosswords.domain.converter.CrosswordConverter;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordRequest;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.service.CrosswordService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupAccessRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class CrosswordControllerImpl implements CrosswordController {

    private final CrosswordService crosswordService;
    private final CrosswordConverter crosswordConverter;
    private final SubjectRepository subjectRepository;
    private final SubjectGroupRepository subjectGroupRepository;
    private final SubjectGroupAccessRepository subjectGroupAccessRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final CrosswordProperties crosswordProperties;

    public CrosswordControllerImpl(CrosswordService crosswordService, CrosswordConverter crosswordConverter,
                                   SubjectRepository subjectRepository,
                                   SubjectGroupRepository subjectGroupRepository,
                                   SubjectGroupAccessRepository subjectGroupAccessRepository,
                                   UserRepository userRepository,
                                   PermissionService permissionService,
                                   CrosswordProperties crosswordProperties) {
        this.crosswordService = crosswordService;
        this.crosswordConverter = crosswordConverter;
        this.subjectRepository = subjectRepository;
        this.subjectGroupRepository = subjectGroupRepository;
        this.subjectGroupAccessRepository = subjectGroupAccessRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.crosswordProperties = crosswordProperties;
    }

    @Override
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of("maxWordLimit", crosswordProperties.getMaxWordLimit()));
    }

    @Override
    public ResponseEntity<?> addCrossword(CrosswordRequest request, User user) {
        permissionService.require(permissionService.canCreate(user, Permission.CROSSWORDS_WRITE));
        Crossword entity = crosswordConverter.toEntity(request);
        entity.setCreatedBy(user);
        Crossword saved = crosswordService.addCrossword(entity);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getCrosswords() {
        return ResponseEntity.ok(crosswordConverter.toResponse(crosswordService.findCrosswords()));
    }

    @Override
    public ResponseEntity<?> getCrossword(UUID id, User user) {
        return ResponseEntity.ok(crosswordConverter.toResponse(crosswordService.findCrossword(id)));
    }

    @Override
    public ResponseEntity<?> updateCrossword(UUID id, CrosswordRequest request, User user) {
        permissionService.require(permissionService.hasCrosswordPermission(user, id, Permission.CROSSWORDS_WRITE));
        Crossword entity = crosswordConverter.toEntity(request);
        crosswordService.updateCrossword(id, entity);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteCrossword(UUID id, User user) {
        permissionService.require(permissionService.hasCrosswordPermission(user, id, Permission.CROSSWORDS_WRITE));
        crosswordService.deleteCrossword(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> addAuthor(UUID id, UUID userId, User user) {
        permissionService.require(permissionService.hasCrosswordPermission(user, id, Permission.CROSSWORDS_WRITE));
        Crossword crossword = crosswordService.addAuthor(id, userId);
        return ResponseEntity.ok(crosswordConverter.toResponse(crossword));
    }

    @Override
    public ResponseEntity<?> removeAuthor(UUID id, UUID userId, User user) {
        permissionService.require(permissionService.hasCrosswordPermission(user, id, Permission.CROSSWORDS_WRITE));
        Crossword crossword = crosswordService.removeAuthor(id, userId);
        return ResponseEntity.ok(crosswordConverter.toResponse(crossword));
    }

    @Override
    public ResponseEntity<?> getAuthorCandidates(UUID id, User user) {
        permissionService.require(permissionService.hasCrosswordPermission(user, id, Permission.CROSSWORDS_WRITE));
        Crossword crossword = crosswordService.findCrossword(id);
        Set<UUID> alreadyAuthors = crossword.getAuthors().stream().map(User::getId).collect(Collectors.toSet());

        Set<UUID> candidateIds = new LinkedHashSet<>();
        subjectRepository.findByCrosswords_Id(id).forEach(subject ->
            subjectGroupRepository.findByTests_Id(subject.getId()).forEach(group ->
                subjectGroupAccessRepository.findBySubjectGroup_IdAndStatus(group.getId(), SubjectGroupAccessStatus.APPROVED)
                    .forEach(req -> candidateIds.add(req.getUser().getId()))
            )
        );
        userRepository.findAll().stream()
                .filter(permissionService::isStaff)
                .map(User::getId).forEach(candidateIds::add);

        List<UserResponse> candidates = candidateIds.stream()
                .filter(cid -> !alreadyAuthors.contains(cid))
                .map(cid -> userRepository.findById(cid).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> UserResponse.builder().id(u.getId()).username(u.getUsername())
                        .role(u.getRole()).profilePictureUrl(u.getProfilePictureUrl()).build())
                .sorted(Comparator.comparing(UserResponse::getUsername))
                .collect(Collectors.toList());
        return ResponseEntity.ok(candidates);
    }

}
