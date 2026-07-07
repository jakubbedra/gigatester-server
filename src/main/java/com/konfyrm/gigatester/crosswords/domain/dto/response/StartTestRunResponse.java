package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StartTestRunResponse {
    private List<TestRunTermResponse> terms;
}
