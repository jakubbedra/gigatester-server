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
@Table(name = "crossword_state_terms")
public class CrosswordStateTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private CrosswordTerm crosswordTerm;

    private int row;

    @Column(name = "col")
    private int column;

    private Direction direction;

}
