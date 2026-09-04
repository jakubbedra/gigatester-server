package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.crosswords.domain.entity.enums.ClueType;
import com.konfyrm.gigatester.tags.entity.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crossword_terms")
public class CrosswordTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String term;

    /**
     * When this term was added, so it can be listed newest-first. Null on rows that
     * predate this field (boxed and left nullable rather than backfilled, per the
     * project's convention for adding columns to existing tables) — treated as
     * "oldest possible" when sorting, so legacy terms sink to the bottom rather than
     * erroring or needing a migration.
     */
    private Instant createdAt;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String clue;

    private ClueType clueType;

    @ManyToMany
    @JoinTable(
        name = "crossword_term_tags",
        joinColumns = @JoinColumn(name = "term_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

}