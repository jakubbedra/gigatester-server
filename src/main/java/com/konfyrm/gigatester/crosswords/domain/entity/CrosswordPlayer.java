package com.konfyrm.gigatester.crosswords.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crossword_players")
public class CrosswordPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String handLetters;

    private int points;

    private boolean current;

    private boolean bot;// todo: bot player is played on backend randomly

}
