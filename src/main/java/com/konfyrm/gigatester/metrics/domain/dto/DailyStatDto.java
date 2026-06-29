package com.konfyrm.gigatester.metrics.domain.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatDto {
    private LocalDate date;
    private int testsTaken;
    private int testsPassed;
    private int questionsAnswered;
    private int questionsCorrect;
}
