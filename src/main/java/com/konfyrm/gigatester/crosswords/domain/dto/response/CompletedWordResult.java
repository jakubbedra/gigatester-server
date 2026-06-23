package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedWordResult {
    private String word;
    private int points;
}
