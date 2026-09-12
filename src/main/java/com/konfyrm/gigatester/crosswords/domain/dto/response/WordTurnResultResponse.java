package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

/** WORDS-mode singleplayer: outcome of one submitWord call — the human's guess, then the bot's turn. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordTurnResultResponse {

    private boolean humanCorrect;
    /** Only set when humanCorrect is true — never reveals the answer to a wrong guess. */
    private String humanWord;
    private int humanPoints;

    /** False only when there were no unsolved words left for the bot to try. */
    private boolean botAttempted;
    private boolean botCorrect;
    /** Only set when botCorrect is true — never reveals the answer to a bot miss. */
    private String botWord;
    private int botPoints;

}
