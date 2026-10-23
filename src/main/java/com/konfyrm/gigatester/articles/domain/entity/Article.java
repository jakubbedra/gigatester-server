package com.konfyrm.gigatester.articles.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.UUID;

/** A piece of theory: rich text (with images and LaTeX) that students read alongside the tests. */
@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    /** HTML from the rich-text editor. */
    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String content;

    /** Drafts are visible to staff only. */
    @Builder.Default
    private boolean published = false;

    /** Optional "practice this" link to a test; a plain id, so a deleted test just stops resolving. */
    private UUID relatedTestId;

    private Instant createdAt;

    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

}
