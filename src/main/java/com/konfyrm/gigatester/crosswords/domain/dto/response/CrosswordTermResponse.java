package com.konfyrm.gigatester.crosswords.domain.dto.response;

import com.konfyrm.gigatester.crosswords.domain.entity.enums.ClueType;
import com.konfyrm.gigatester.tags.dto.TagResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordTermResponse {

    private UUID id;

    private String term;

    private String clue;

    private ClueType clueType;

    private List<TagResponse> tags;

}
