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

    /** Null for a genuinely new term; set to an existing term's id when editing it in place. */
    private UUID id;

    private String term;

    private String clue;

    private ClueType clueType;

    private List<UUID> tagIds;

}
