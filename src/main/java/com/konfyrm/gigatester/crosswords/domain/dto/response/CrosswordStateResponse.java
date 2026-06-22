package com.konfyrm.gigatester.crosswords.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordStateResponse {

    private UUID id;

    private String currentGrid;

    private int width;

    private int height;

    private List<CrosswordPlayerResponse> players;

    private List<CrosswordStateClueResponse> clues;

}
