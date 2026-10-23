package com.konfyrm.gigatester.articles.domain.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummaryResponse {
    private UUID id;
    private String name;
    private boolean published;
}
