package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CompleteTestRunRequest {
    private List<UUID> wrongTermIds;
    private int totalTerms;
}
