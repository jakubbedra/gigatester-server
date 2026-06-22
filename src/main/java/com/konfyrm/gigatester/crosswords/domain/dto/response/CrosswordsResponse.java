package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordsResponse {

    private List<CrosswordSummaryResponse> crosswords;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrosswordSummaryResponse {
        private UUID id;
        private String name;
        private int termCount;
    }

}
