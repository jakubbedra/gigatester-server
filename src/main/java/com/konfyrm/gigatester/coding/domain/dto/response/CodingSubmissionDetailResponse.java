package com.konfyrm.gigatester.coding.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** A stored submission with its code, for staff review. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingSubmissionDetailResponse {

    private UUID id;
    private UUID taskId;
    private String problemTitle;
    private String username;
    private String code;
    private int passed;
    private int total;
    private double score;
    private double maxScore;
    private Instant submittedAt;

}
