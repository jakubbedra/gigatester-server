package com.konfyrm.gigatester.coding.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingSubmissionResponse {

    private UUID submissionId;
    private int passed;
    private int total;
    private double score;
    private double maxScore;
    private List<Boolean> results;
    /** This submission finished the test (every problem now has an attempt). */
    private boolean completed;

}
