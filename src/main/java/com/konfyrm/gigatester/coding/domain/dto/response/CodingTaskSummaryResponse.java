package com.konfyrm.gigatester.coding.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTaskSummaryResponse {
    private UUID id;
    private String name;
    private Instant startAt;
    private Instant windowEnd;
    private int problemCount;
}
