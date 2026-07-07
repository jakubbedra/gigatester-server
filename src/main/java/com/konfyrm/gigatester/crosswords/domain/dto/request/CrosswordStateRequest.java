package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordStateRequest {

    private UUID crosswordId;

    private int numberOfWords;

    private List<UUID> tagFilter;

    private String tagFilterMode;

    private List<UUID> termIdFilter;

}