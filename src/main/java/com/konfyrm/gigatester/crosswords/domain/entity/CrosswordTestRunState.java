package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crossword_test_run_states")
@Getter @Setter @Builder(toBuilder = true) @NoArgsConstructor @AllArgsConstructor
public class CrosswordTestRunState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crossword_id")
    private Crossword crossword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Comma-joined term ids, in the shuffled order presented to the user — fixed for the life of this run. */
    @Column(columnDefinition = "text")
    private String termIds;

    /** "ALL" or "WRONG_ONLY" — the mode this run was started with. */
    private String mode;

    private int currentIndex;

    @Column(columnDefinition = "text")
    private String wrongTermIds;

}
