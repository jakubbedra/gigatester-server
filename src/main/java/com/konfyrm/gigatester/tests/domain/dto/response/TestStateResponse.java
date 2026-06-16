package com.konfyrm.gigatester.tests.domain.dto.response;

import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestDisplayTypeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestExecutionStateDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStateResponse {

    private UUID id;

    private String testName;

    private List<QuestionStateSummaryResponse> questions;

    private int closedQuestionsCount;

    private int openQuestionsCount;

    private int termDefinitionQuestionsCount;

    private int currentQuestionsCount;

    private int currentQuestionIndex;

    private double passingPercentage;

    private TestModeDto mode;

    private TestDisplayTypeDto displayType;

    private TestExecutionStateDto executionState;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionStateSummaryResponse {

        private UUID id;

        private UUID questionId;

        private QuestionType questionType;

    }

}
