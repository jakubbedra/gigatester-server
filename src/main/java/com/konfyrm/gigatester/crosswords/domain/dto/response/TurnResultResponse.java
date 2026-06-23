package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnResultResponse {

    private List<TurnCellResult> humanResults;
    private int humanWordBonus;
    private List<CompletedWordResult> humanCompletedWords;

    private List<TurnCellResult> botPlacements;
    private int botWordBonus;

}
