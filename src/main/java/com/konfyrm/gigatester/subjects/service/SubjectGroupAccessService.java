package com.konfyrm.gigatester.subjects.service;

import com.konfyrm.gigatester.subjects.domain.dto.response.AccessRequestResponse;
import com.konfyrm.gigatester.subjects.domain.dto.response.MyAccessStatusResponse;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessRequest;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupAccessRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SubjectGroupAccessService {

    private final SubjectGroupAccessRepository accessRepository;
    private final SubjectGroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    public SubjectGroupAccessService(SubjectGroupAccessRepository accessRepository,
                                     SubjectGroupRepository groupRepository,
                                     SubjectRepository subjectRepository,
                                     PermissionService permissionService,
                                     UserRepository userRepository) {
        this.userRepository = userRepository;
        this.accessRepository = accessRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.permissionService = permissionService;
    }

    public void requestAccess(UUID groupId, User user) {
        SubjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        accessRepository.findByUser_IdAndSubjectGroup_Id(user.getId(), groupId)
                .ifPresentOrElse(
                        req -> {
                            req.setStatus(SubjectGroupAccessStatus.PENDING);
                            accessRepository.save(req);
                        },
                        () -> accessRepository.save(SubjectGroupAccessRequest.builder()
                                .user(user)
                                .subjectGroup(group)
                                .status(SubjectGroupAccessStatus.PENDING)
                                .build())
                );
    }

    public List<MyAccessStatusResponse> getMyAccessStatuses(UUID userId) {
        return accessRepository.findByUser_Id(userId).stream()
                .map(r -> new MyAccessStatusResponse(r.getSubjectGroup().getId(), r.getStatus()))
                .toList();
    }

    public List<AccessRequestResponse> getPendingRequestsForGroup(UUID groupId) {
        return accessRepository.findBySubjectGroup_IdAndStatus(groupId, SubjectGroupAccessStatus.PENDING)
                .stream()
                .map(r -> new AccessRequestResponse(
                        r.getId(), r.getUser().getId(), r.getUser().getUsername(),
                        r.getSubjectGroup().getId(), r.getSubjectGroup().getName(), r.getStatus()))
                .toList();
    }

    public List<AccessRequestResponse> getAccessRequestsForGroup(UUID groupId) {
        return accessRepository.findBySubjectGroup_Id(groupId)
                .stream()
                .filter(r -> r.getStatus() != SubjectGroupAccessStatus.DENIED)
                .map(r -> new AccessRequestResponse(
                        r.getId(), r.getUser().getId(), r.getUser().getUsername(),
                        r.getSubjectGroup().getId(), r.getSubjectGroup().getName(), r.getStatus()))
                .toList();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AccessRequestResponse> getAllAccessRequests() {
        return accessRepository.findAll().stream()
                .map(r -> new AccessRequestResponse(
                        r.getId(), r.getUser().getId(), r.getUser().getUsername(),
                        r.getSubjectGroup().getId(), r.getSubjectGroup().getName(), r.getStatus()))
                .toList();
    }

    /**
     * Admin shortcut: puts a user straight into a group with approved access, whatever state any
     * earlier request was in (pending, denied or none at all).
     */
    public AccessRequestResponse addMember(UUID groupId, UUID userId) {
        SubjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SubjectGroupAccessRequest request = accessRepository.findByUser_IdAndSubjectGroup_Id(userId, groupId)
                .orElseGet(() -> SubjectGroupAccessRequest.builder().user(user).subjectGroup(group).build());
        request.setStatus(SubjectGroupAccessStatus.APPROVED);
        request = accessRepository.save(request);
        return new AccessRequestResponse(request.getId(), user.getId(), user.getUsername(),
                group.getId(), group.getName(), request.getStatus());
    }

    public void revokeAccess(UUID requestId) {
        SubjectGroupAccessRequest req = accessRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        accessRepository.delete(req);
    }

    public void approve(UUID requestId) {
        SubjectGroupAccessRequest req = accessRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        req.setStatus(SubjectGroupAccessStatus.APPROVED);
        accessRepository.save(req);
    }

    public void deny(UUID requestId) {
        SubjectGroupAccessRequest req = accessRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        req.setStatus(SubjectGroupAccessStatus.DENIED);
        accessRepository.save(req);
    }

    public boolean hasAccess(UUID groupId, User user) {
        return accessRepository.findByUser_IdAndSubjectGroup_Id(user.getId(), groupId)
                .map(r -> r.getStatus() == SubjectGroupAccessStatus.APPROVED)
                .orElse(false);
    }

    public boolean hasAccessToTest(UUID testId, User user) {
        if (permissionService.isStaff(user)) return true;
        List<Subject> subjects = subjectRepository.findByTests_Id(testId);
        for (Subject subject : subjects) {
            for (SubjectGroup group : groupRepository.findByTests_Id(subject.getId())) {
                if (hasAccess(group.getId(), user)) return true;
            }
        }
        return false;
    }

    public boolean isOwnerOfRequest(UUID requestId, UUID userId) {
        return accessRepository.findById(requestId)
                .map(r -> r.getSubjectGroup().getOwners().stream().anyMatch(u -> u.getId().equals(userId)))
                .orElse(false);
    }

    public boolean hasAccessToCrossword(UUID crosswordId, User user) {
        if (permissionService.isStaff(user)) return true;
        List<Subject> subjects = subjectRepository.findByCrosswords_Id(crosswordId);
        for (Subject subject : subjects) {
            for (SubjectGroup group : groupRepository.findByTests_Id(subject.getId())) {
                if (hasAccess(group.getId(), user)) return true;
            }
        }
        return false;
    }

    public boolean hasAccessToCodingTask(UUID taskId, User user) {
        if (permissionService.isStaff(user)) return true;
        for (Subject subject : subjectRepository.findByCodingTasks_Id(taskId)) {
            for (SubjectGroup group : groupRepository.findByTests_Id(subject.getId())) {
                if (hasAccess(group.getId(), user)) return true;
            }
        }
        return false;
    }

    public boolean hasAccessToGraph(UUID graphId, User user) {
        if (permissionService.isStaff(user)) return true;
        List<Subject> subjects = subjectRepository.findByGraphs_Id(graphId);
        for (Subject subject : subjects) {
            for (SubjectGroup group : groupRepository.findByTests_Id(subject.getId())) {
                if (hasAccess(group.getId(), user)) return true;
            }
        }
        return false;
    }
}
