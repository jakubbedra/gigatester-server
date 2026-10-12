package com.konfyrm.gigatester.tests.domain.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGradeRequest {

    private List<Grade> grades;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Grade {

        private UUID questionStateId;

        private double score;

    }

}
