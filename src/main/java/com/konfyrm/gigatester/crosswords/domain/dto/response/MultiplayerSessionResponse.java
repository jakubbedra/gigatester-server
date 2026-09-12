package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiplayerSessionResponse {

    private UUID id;
    private UUID crosswordId;
    private String crosswordName;

    private PlayerInfo player1;
    private PlayerInfo player2;

    private String status;
    private String mode;
    private UUID currentTurnUserId;
    private boolean myTurn;

    /** Set only on the response to a WORDS-mode word submission; null otherwise. */
    private Boolean lastGuessCorrect;
    private String lastGuessWord;
    private Integer lastGuessPoints;

    private String currentGrid;
    private int width;
    private int height;

    private String myHand;
    private int player1Points;
    private int player2Points;

    private List<CrosswordStateClueResponse> clues;

    private LocalDateTime createdAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerInfo {
        private UUID id;
        private String username;
        private String profilePictureUrl;
    }

}
