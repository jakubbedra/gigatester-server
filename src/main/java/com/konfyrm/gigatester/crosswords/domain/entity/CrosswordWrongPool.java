package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Tracks, per (user, crossword), the cumulative set of terms currently answered
 * wrong — a term stays in this pool across runs until it's answered correctly,
 * at which point it's removed. Feeds "Retry wrong answers" and "Generate crossword
 * from wrong answers"; does NOT affect the "Start test run" pool, which is always
 * the full (or tag-filtered) term set regardless of past results.
 */
@Entity
@Table(
    name = "crossword_wrong_pools",
    uniqueConstraints = @UniqueConstraint(columnNames = {"crossword_id", "user_id"})
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CrosswordWrongPool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crossword_id")
    private Crossword crossword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text")
    private String wrongTermIds;

}
