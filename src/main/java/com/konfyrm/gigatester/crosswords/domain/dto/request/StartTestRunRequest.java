package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartTestRunRequest {
    private String mode; // "ALL" or "WRONG_ONLY"
}
