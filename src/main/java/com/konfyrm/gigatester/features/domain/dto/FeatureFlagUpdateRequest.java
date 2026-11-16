package com.konfyrm.gigatester.features.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeatureFlagUpdateRequest {
    private boolean enabled;
}
