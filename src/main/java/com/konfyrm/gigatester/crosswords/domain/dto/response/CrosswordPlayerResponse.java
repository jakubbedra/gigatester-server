package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordPlayerResponse {

    private UUID id;

    private String handLetters;

    private int points;

    private boolean current;

    private boolean bot;

}
