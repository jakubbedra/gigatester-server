package com.konfyrm.gigatester.crosswords.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crossword_states")
public class CrosswordState {

    public static final char INACTIVE_FIELD = '#';
    public static final char UNCOVERED_FIELD = '_';

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String solutionGrid;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String currentGrid;

    private int width;

    private int height;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "crossword_state_id")
    private List<CrosswordPlayer> players;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "crossword_state_id")
    private List<CrosswordStateTerm> terms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crossword_id")
    private Crossword crossword;

    public int index(int row, int col) {
        return row * width + col;
    }

    public char solutionAt(int row, int col) {
        return solutionGrid.charAt(index(row, col));
    }

    public char currentAt(int row, int col) {
        return currentGrid.charAt(index(row, col));
    }

    public void setCurrentAt(int row, int col, char value) {
        var sb = new StringBuilder(currentGrid);
        sb.setCharAt(index(row, col), value);
        currentGrid = sb.toString();
    }

}