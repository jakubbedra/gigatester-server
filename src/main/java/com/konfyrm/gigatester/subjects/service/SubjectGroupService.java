package com.konfyrm.gigatester.subjects.service;

import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubjectGroupService {

    private final SubjectGroupRepository subjectGroupRepository;
    private final UserRepository userRepository;

    public SubjectGroupService(SubjectGroupRepository subjectGroupRepository,
                               UserRepository userRepository) {
        this.subjectGroupRepository = subjectGroupRepository;
        this.userRepository = userRepository;
    }

    public SubjectGroup addSubjectGroup(SubjectGroup subjectGroup) {
        return subjectGroupRepository.save(subjectGroup);
    }

    public List<SubjectGroup> findSubjectGroups() {
        return subjectGroupRepository.findAll();
    }

    public SubjectGroup findSubjectGroup(UUID id) {
        return subjectGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubjectGroup with id: " + id + " not found."));
    }

    public void updateSubjectGroup(UUID id, SubjectGroup subjectGroup) {
        Optional<SubjectGroup> existing = subjectGroupRepository.findById(id);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubjectGroup with id: " + id + " not found.");
        }
        subjectGroupRepository.save(existing.get().toBuilder()
                .name(subjectGroup.getName())
                .tests(subjectGroup.getTests())
                .build());
    }

    public void deleteSubjectGroup(UUID id) {
        if (subjectGroupRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubjectGroup with id: " + id + " not found.");
        }
        subjectGroupRepository.deleteById(id);
    }

    @Transactional
    public SubjectGroup addOwner(UUID groupId, UUID userId) {
        SubjectGroup group = subjectGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (group.getOwners().stream().noneMatch(u -> u.getId().equals(userId))) {
            group.getOwners().add(user);
            subjectGroupRepository.save(group);
        }
        return group;
    }

    @Transactional
    public SubjectGroup removeOwner(UUID groupId, UUID userId) {
        SubjectGroup group = subjectGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        group.getOwners().removeIf(u -> u.getId().equals(userId));
        return subjectGroupRepository.save(group);
    }

    public List<UserResponse> getOwnerCandidates(UUID groupId) {
        SubjectGroup group = subjectGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Set<UUID> existingOwnerIds = group.getOwners().stream()
                .map(User::getId).collect(Collectors.toSet());
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.MODERATOR || u.getRole() == UserRole.ADMIN)
                .filter(u -> !existingOwnerIds.contains(u.getId()))
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .role(u.getRole())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .build())
                .toList();
    }

    public boolean isOwner(UUID groupId, UUID userId) {
        return subjectGroupRepository.findById(groupId)
                .map(g -> g.getOwners().stream().anyMatch(u -> u.getId().equals(userId)))
                .orElse(false);
    }

}
