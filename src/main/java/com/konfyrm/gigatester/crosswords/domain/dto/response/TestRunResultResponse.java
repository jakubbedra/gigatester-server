package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TestRunResultResponse {
    private UUID id;
    private List<UUID> wrongTermIds;
    private int wrongTermCount;
    private int totalTerms;
    private LocalDateTime createdAt;
}
