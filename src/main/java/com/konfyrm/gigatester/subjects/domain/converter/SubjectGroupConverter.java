package com.konfyrm.gigatester.subjects.domain.converter;

import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectGroupRequest;
import com.konfyrm.gigatester.subjects.domain.dto.response.SubjectGroupResponse;
import com.konfyrm.gigatester.subjects.domain.dto.response.SubjectGroupsResponse;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubjectGroupConverter {

    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectGroupConverter(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectGroup toEntity(SubjectGroupRequest request) {
        return SubjectGroup.builder()
                .name(request.getName())
                .tests(request.getSubjects().stream()
                        .map(id -> subjectRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Subject not found for id: " + id)))
                        .toList())
                .build();
    }

    public SubjectGroupResponse toResponse(SubjectGroup group) {
        return SubjectGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .subjects(group.getTests().stream().map(Subject::getId).toList())
                .owners(group.getOwners().stream().map(this::toUserResponse).toList())
                .build();
    }

    private UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .role(u.getRole())
                .profilePictureUrl(u.getProfilePictureUrl())
                .build();
    }

    public SubjectGroupsResponse toResponse(List<SubjectGroup> groups) {
        return new SubjectGroupsResponse(groups.stream()
                .map(g -> SubjectGroupsResponse.SubjectGroupSummaryResponse.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .build())
                .toList());
    }

}
