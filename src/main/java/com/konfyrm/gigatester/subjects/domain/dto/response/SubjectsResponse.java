package com.konfyrm.gigatester.subjects.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectsResponse {

    private List<SubjectSummaryResponse> subjects;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectSummaryResponse {
        private UUID id;
        private String name;
    }

}