package com.konfyrm.gigatester.subjects.domain.dto.response;

import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccessRequestResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID groupId;
    private String groupName;
    private SubjectGroupAccessStatus status;
}
