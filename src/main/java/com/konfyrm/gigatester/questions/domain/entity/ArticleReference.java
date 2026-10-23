package com.konfyrm.gigatester.questions.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** A pointer from a question to an article, optionally to one section (heading) of it. */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleReference {

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    /** Text of the target heading; null/blank means the whole article. */
    @Column(length = 300)
    private String section;
}
