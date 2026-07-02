package com.konfyrm.gigatester.metrics.domain.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagAccuracyDto {
    private UUID tagId;
    private String tagKey;
    private int timesAnswered;
    private int timesCorrect;
    private double accuracy;
}
