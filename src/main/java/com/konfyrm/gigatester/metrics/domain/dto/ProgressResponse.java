package com.konfyrm.gigatester.metrics.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    private int totalTestsTaken;
    private int totalTestsPassed;
    private int totalQuestionsAnswered;
    private int totalQuestionsCorrect;
    private List<DailyStatDto> dailyStats;
    private List<TestSummaryForMetricsDto> myTests;
    private List<TagAccuracyDto> tagStats;
}
