package com.konfyrm.gigatester.features.controller;

import com.konfyrm.gigatester.features.domain.dto.FeatureFlagResponse;
import com.konfyrm.gigatester.features.domain.dto.FeatureFlagUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/feature-flags")
public interface FeatureFlagController {

    /** Any signed-in user may read the flags: the UI needs them to decide what to show. */
    @GetMapping
    ResponseEntity<List<FeatureFlagResponse>> list();

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{key}")
    ResponseEntity<FeatureFlagResponse> update(@PathVariable String key, @RequestBody FeatureFlagUpdateRequest request);
}
