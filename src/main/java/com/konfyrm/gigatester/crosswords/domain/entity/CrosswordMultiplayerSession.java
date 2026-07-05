package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crossword_multiplayer_sessions")
public class CrosswordMultiplayerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Crossword crossword;

    @ManyToOne
    private User player1;

    @ManyToOne
    private User player2;

    @Enumerated(EnumType.STRING)
    private CrosswordMultiplayerStatus status;

    private UUID currentTurnUserId;

    @Column(columnDefinition = "text")
    private String solutionGrid;

    @Column(columnDefinition = "text")
    private String currentGrid;

    private int width;
    private int height;

    private String player1Hand;
    private int player1Points;

    private String player2Hand;
    private int player2Points;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "session_id")
    @Builder.Default
    private List<CrosswordMultiplayerTerm> terms = new ArrayList<>();

    private LocalDateTime createdAt;

    // ── grid helpers ─────────────────────────────────────────────────────────

    public char solutionAt(int row, int col) {
        return solutionGrid.charAt(row * width + col);
    }

    public char currentAt(int row, int col) {
        return currentGrid.charAt(row * width + col);
    }

    public void setCurrentAt(int row, int col, char c) {
        int idx = row * width + col;
        currentGrid = currentGrid.substring(0, idx) + c + currentGrid.substring(idx + 1);
    }

}
