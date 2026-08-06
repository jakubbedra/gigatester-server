package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StartTestRunResponse {
    private List<TestRunTermResponse> terms;
    /** Index of the first unanswered term — 0 for a fresh run, >0 when resuming an in-progress run. */
    private int resumedIndex;
    private List<UUID> resumedWrongTermIds;
}
