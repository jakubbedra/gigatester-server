package com.konfyrm.gigatester.crosswords.domain.dto.response;

import com.konfyrm.gigatester.crosswords.domain.dto.enums.DirectionDto;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.ClueType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordStateClueResponse {

    private UUID id;

    private String clue;

    private ClueType clueType;

    private int row;

    private int column;

    private DirectionDto direction;

    /** WORDS-mode multiplayer: true once this word has been correctly guessed and locked. Always false elsewhere. */
    private boolean solved;

}
