package com.konfyrm.gigatester.subjects.domain.dto.response;

import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MyAccessStatusResponse {
    private UUID groupId;
    private SubjectGroupAccessStatus status;
}
