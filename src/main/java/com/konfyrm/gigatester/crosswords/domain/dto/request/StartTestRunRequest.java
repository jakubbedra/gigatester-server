package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartTestRunRequest {
    private String mode; // "ALL" or "WRONG_ONLY"

    /** True to resume an in-progress run of the same mode if one exists; false always starts fresh, discarding it. */
    private boolean resume;
}
