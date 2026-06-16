package com.konfyrm.gigatester.tests.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestsResponse {

    private List<TestSummaryResponse> tests;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSummaryResponse {

        private UUID id;

        private String name;

    }

}
