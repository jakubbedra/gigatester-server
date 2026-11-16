package com.konfyrm.gigatester.features.controller;

import com.konfyrm.gigatester.features.domain.FeatureFlag;
import com.konfyrm.gigatester.features.domain.dto.FeatureFlagResponse;
import com.konfyrm.gigatester.features.domain.dto.FeatureFlagUpdateRequest;
import com.konfyrm.gigatester.features.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeatureFlagControllerImpl implements FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @Override
    public ResponseEntity<List<FeatureFlagResponse>> list() {
        return ResponseEntity.ok(Arrays.stream(FeatureFlag.values()).map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<FeatureFlagResponse> update(String key, FeatureFlagUpdateRequest request) {
        FeatureFlag flag = Arrays.stream(FeatureFlag.values())
                .filter(f -> f.name().equals(key))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        featureFlagService.set(flag, request.isEnabled());
        return ResponseEntity.ok(toResponse(flag));
    }

    private FeatureFlagResponse toResponse(FeatureFlag flag) {
        return FeatureFlagResponse.builder()
                .key(flag.name())
                .label(flag.getLabel())
                .description(flag.getDescription())
                .enabled(featureFlagService.isEnabled(flag))
                .defaultValue(flag.getDefaultValue())
                .build();
    }
}
