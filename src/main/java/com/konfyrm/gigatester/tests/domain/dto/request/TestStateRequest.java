package com.konfyrm.gigatester.tests.domain.dto.request;

import com.konfyrm.gigatester.tests.domain.dto.enums.TestDisplayTypeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import jakarta.annotation.Nullable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStateRequest {

    private int closedQuestionsCount;

    private int openQuestionsCount;

    private int statementQuestionsCount;

    @Nullable
    private Double passingPercentage;

    private TestModeDto mode;

    private TestDisplayTypeDto displayType;

    private boolean timeLimitEnabled;
    //todo: coming soon...
    // {startDate: xxx, timeLimitMs: xxx}

}