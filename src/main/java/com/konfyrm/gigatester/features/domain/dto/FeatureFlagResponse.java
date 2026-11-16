package com.konfyrm.gigatester.features.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeatureFlagResponse {
    private String key;
    private String label;
    private String description;
    private boolean enabled;
    private boolean defaultValue;
}
