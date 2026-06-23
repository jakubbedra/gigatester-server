package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordStateUpdateRequest {

    private List<CrosswordLetterRequest> letters;

}