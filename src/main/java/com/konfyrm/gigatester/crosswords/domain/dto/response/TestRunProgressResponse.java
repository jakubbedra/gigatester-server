package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestRunProgressResponse {
    private String mode;
    private int currentIndex;
    private int totalTerms;
    private int wrongTermCount;
}
