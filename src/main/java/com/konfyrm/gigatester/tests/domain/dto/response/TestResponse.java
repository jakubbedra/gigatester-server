package com.konfyrm.gigatester.tests.domain.dto.response;

import lombok.*;

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

    private double passingPercentage;

}
