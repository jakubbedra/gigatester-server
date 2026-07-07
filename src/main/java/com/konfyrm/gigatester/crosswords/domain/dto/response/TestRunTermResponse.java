package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TestRunTermResponse {
    private UUID id;
    private String clue;
    private String clueType;
    private String term;
    private int termLength;
}
