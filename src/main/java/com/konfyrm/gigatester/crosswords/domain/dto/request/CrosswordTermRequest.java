package com.konfyrm.gigatester.crosswords.domain.dto.request;

import com.konfyrm.gigatester.crosswords.domain.entity.enums.ClueType;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordTermRequest {

    private String term;

    private String clue;

    private ClueType clueType;

    private List<UUID> tagIds;

}
