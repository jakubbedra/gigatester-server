package com.konfyrm.gigatester.tests.domain.dto.response;

import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
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
public class TestResponse {

    private UUID id;

    private String name;

    private List<UUID> questions;

    private int closedQuestionsCount;

    private int openQuestionsCount;

    private int storedClosedQuestionsCount;

    private int storedOpenQuestionsCount;

    private double passingPercentage;

    private long timeLimit;

    private boolean hideTagsDuringTest;

    private TestType type;

    private Instant startAt;

    private Instant windowEnd;

    private List<UserResponse> authors;

}
