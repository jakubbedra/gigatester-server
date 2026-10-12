package com.konfyrm.gigatester.coding.domain.dto.request;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTaskRequest {

    private String name;
    /** Optional intro shown above the problems. */
    private String description;
    /** Both null = always available. */
    private Instant startAt;
    private Instant windowEnd;
    private List<ProblemDto> problems;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemDto {
        /** Null for a new problem; an existing id keeps that problem (and its submissions). */
        private UUID id;
        private String title;
        private String description;
        private String starterCode;
        private double points;
        private long timeLimitMs;
        private List<TestCaseDto> tests;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseDto {
        private String input;
        private String expectedOutput;
        private boolean hidden;
    }

}
