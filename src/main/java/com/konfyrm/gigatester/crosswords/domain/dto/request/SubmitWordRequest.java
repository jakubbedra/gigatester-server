package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.*;

import java.util.UUID;

/** WORDS-mode multiplayer: the clue the player is answering plus the whole word they typed. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitWordRequest {

    /** Id of the {@code CrosswordMultiplayerTerm} being answered (matches CrosswordStateClueResponse.id). */
    private UUID termId;

    private String word;

}
