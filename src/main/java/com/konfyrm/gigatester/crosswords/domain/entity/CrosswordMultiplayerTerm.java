package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.crosswords.domain.entity.enums.Direction;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crossword_multiplayer_terms")
public class CrosswordMultiplayerTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private CrosswordTerm crosswordTerm;

    private int row;

    @Column(name = "col")
    private int column;

    private Direction direction;

    /**
     * WORDS mode only: true once a player has correctly typed this whole word.
     * Boxed and null-tolerant so rows created before this column existed still read
     * (null == not solved). Locks the word so it can't be scored twice.
     */
    private Boolean solved;

    public boolean isSolved() {
        return Boolean.TRUE.equals(solved);
    }

}
