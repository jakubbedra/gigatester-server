package com.konfyrm.gigatester.tests.domain.dto.request;

import com.konfyrm.gigatester.tests.domain.entity.TestType;
import lombok.*;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {

    private String name;

    private List<UUID> questions;

    private int closedQuestionsCount;

    private int openQuestionsCount;

    private double passingPercentage;

    private long timeLimit;

    private boolean hideTagsDuringTest;

    /** Optional; defaults to STANDARD. */
    private TestType type;

    private Instant startAt;

    private Instant windowEnd;

}
