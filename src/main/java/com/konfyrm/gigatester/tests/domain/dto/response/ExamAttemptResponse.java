package com.konfyrm.gigatester.tests.domain.dto.response;

import lombok.*;

import java.util.UUID;

/** One row of the staff "who took this exam" list. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptResponse {

    private UUID stateId;

    private UUID userId;

    private String username;

    /** IN_PROGRESS or FINISHED. */
    private String executionState;

    private Long startedAt;

    private Long submittedAt;

    private double totalScore;

    private double maxScore;

    /** Manual open questions staff has not graded yet. */
    private int pendingGrading;

}
