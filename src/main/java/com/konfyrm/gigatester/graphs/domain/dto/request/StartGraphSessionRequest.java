package com.konfyrm.gigatester.graphs.domain.dto.request;

import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphDifficulty;
import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphPlayMode;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartGraphSessionRequest {
    private UUID graphId;
    private GraphDifficulty difficulty;
    /** Optional; defaults to TYPING. */
    private GraphPlayMode mode;
}
