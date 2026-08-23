package com.konfyrm.gigatester.tests.domain.dto.request;

import com.konfyrm.gigatester.tests.domain.dto.enums.TestDisplayTypeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestQuestionDistributionMode;
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

    @Builder.Default
    private TestQuestionDistributionMode distributionMode = TestQuestionDistributionMode.RANDOM;

    /** Only used when distributionMode is MAX_PER_TAG: max questions drawn per selected tag. */
    private int maxPerTag;

}