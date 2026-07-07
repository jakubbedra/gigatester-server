package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crossword_test_runs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CrosswordTestRun {

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

    private int totalTerms;

    private boolean completed;

    private LocalDateTime createdAt;
}
