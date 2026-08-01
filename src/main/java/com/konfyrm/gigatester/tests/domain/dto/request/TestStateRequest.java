package com.konfyrm.gigatester.tests.domain.dto.request;

import com.konfyrm.gigatester.tests.domain.dto.enums.TestDisplayTypeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private long timeLimitMs;

    @Builder.Default
    private List<UUID> tagIds = new ArrayList<>();

    private boolean excludeTags;

    /** AND mode when true (must have ALL tagIds); OR mode when false (must have at least one). */
    private boolean matchAllTags;

}