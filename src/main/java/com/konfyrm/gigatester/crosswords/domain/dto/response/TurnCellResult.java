package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnCellResult {

    private int row;
    private int column;
    private char letter;
    private boolean correct;

}
