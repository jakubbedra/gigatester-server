package com.konfyrm.gigatester.articles.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private UUID id;
    private String name;
    private String content;
    private boolean published;
    private UUID relatedTestId;
    private String relatedTestName;
    private Instant updatedAt;
    private String authorName;
    /** The caller may edit this article. */
    private boolean canEdit;
}
