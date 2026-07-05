package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiplayerSessionSummaryResponse {

    private UUID id;
    private UUID crosswordId;
    private String crosswordName;
    private MultiplayerSessionResponse.PlayerInfo opponent;
    private String status;
    private boolean myTurn;
    private int myScore;
    private int opponentScore;
    private LocalDateTime createdAt;

}
