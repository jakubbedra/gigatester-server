package com.konfyrm.gigatester.crosswords.domain.dto.request;

import com.konfyrm.gigatester.crosswords.domain.entity.enums.BotDifficulty;
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

    private BotDifficulty botDifficulty;

}