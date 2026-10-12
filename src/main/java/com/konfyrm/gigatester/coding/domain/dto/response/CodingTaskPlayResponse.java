package com.konfyrm.gigatester.coding.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * What a student gets. Before a scheduled test opens, only the schedule is sent (no problems).
 * Hidden tests carry their input (the browser has to run it) but never the expected output.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTaskPlayResponse {

    private UUID id;
    private String name;
    private String description;
    private Instant startAt;
    private Instant windowEnd;
    /** open | notStarted | closed */
    private String phase;
    private double maxScore;
    /** Sum of the best score on each problem. */
    private double totalScore;
    /** The student has submitted every problem; the test is finished and read-only for them. */
    private boolean completed;
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
        private Double bestScore;
        private Integer bestPassed;
        private int attempts;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        private String input;
        /** Null for hidden tests. */
        private String expectedOutput;
        private boolean hidden;
    }

}
