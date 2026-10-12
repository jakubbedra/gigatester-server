package com.konfyrm.gigatester.tests.domain.dto.response;

import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import lombok.*;

import java.util.List;
import java.util.UUID;

/** A whole attempt laid out for grading (staff) or review (the student, once the window has closed). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGradingResponse {

    private UUID stateId;

    private UUID testId;

    private String testName;

    private UUID userId;

    private String username;

    private String executionState;

    private Long submittedAt;

    private double totalScore;

    private double maxScore;

    private double passingPercentage;

    /** True when the caller may change scores (staff); false for the student's read-only review. */
    private boolean canGrade;

    private List<Item> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        private UUID questionStateId;

        private UUID questionId;

        private QuestionType questionType;

        private double maxScore;

        private double score;

        private boolean answered;

        /** An open question flagged for manual grading. */
        private boolean manual;

        /** Staff has set or confirmed this score. */
        private boolean graded;

    }

}
