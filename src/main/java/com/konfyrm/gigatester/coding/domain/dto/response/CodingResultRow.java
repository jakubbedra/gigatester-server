package com.konfyrm.gigatester.coding.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One student in the staff results table: the sum of their best attempts, with a per-problem breakdown. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingResultRow {

    private UUID userId;
    private String username;
    private double totalScore;
    private double maxScore;
    private int attempts;
    private Instant lastSubmittedAt;
    private List<ProblemResult> problems;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemResult {
        private UUID problemId;
        private String title;
        private UUID bestSubmissionId;
        private double bestScore;
        private double maxScore;
        private int bestPassed;
        private int total;
        private int attempts;
    }

}
