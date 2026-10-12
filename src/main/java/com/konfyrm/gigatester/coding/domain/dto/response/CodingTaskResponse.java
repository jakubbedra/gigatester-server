package com.konfyrm.gigatester.coding.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Full programming test for staff editing, expected outputs included. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTaskResponse {

    private UUID id;
    private String name;
    private String description;
    private Instant startAt;
    private Instant windowEnd;
    private List<Problem> problems;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Problem {
        private UUID id;
        private String title;
        private String description;
        private String starterCode;
        private double points;
        private long timeLimitMs;
        private List<TestCase> tests;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        private String input;
        private String expectedOutput;
        private boolean hidden;
    }

}
