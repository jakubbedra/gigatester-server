package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordLetterRequest {

    private char c;

    private int row;

    private int column;

}