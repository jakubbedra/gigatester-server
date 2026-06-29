package com.konfyrm.gigatester.metrics.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntryDto {
    private int rank;
    private String username;
    private int totalTestsTaken;
    private int totalTestsPassed;
    private double passRate;
    private int totalQuestionsCorrect;
}
