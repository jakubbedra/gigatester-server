package com.konfyrm.gigatester.articles.domain.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {
    private String name;
    private String content;
    private boolean published;
    private UUID relatedTestId;
}
