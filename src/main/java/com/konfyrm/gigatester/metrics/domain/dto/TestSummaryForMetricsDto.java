package com.konfyrm.gigatester.metrics.domain.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSummaryForMetricsDto {
    private UUID id;
    private String name;
}
