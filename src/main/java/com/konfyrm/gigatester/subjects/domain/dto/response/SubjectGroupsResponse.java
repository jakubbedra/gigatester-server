package com.konfyrm.gigatester.subjects.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGroupsResponse {

    private List<SubjectGroupSummaryResponse> subjectGroups;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectGroupSummaryResponse {
        private UUID id;
        private String name;
    }

}
