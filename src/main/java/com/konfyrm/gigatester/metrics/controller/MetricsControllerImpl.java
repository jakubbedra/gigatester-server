package com.konfyrm.gigatester.metrics.controller;

import com.konfyrm.gigatester.metrics.domain.dto.ProgressResponse;
import com.konfyrm.gigatester.metrics.domain.dto.RankingEntryDto;
import com.konfyrm.gigatester.metrics.domain.dto.TagAccuracyDto;
import com.konfyrm.gigatester.metrics.service.MetricsService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class MetricsControllerImpl implements MetricsController {

    private final MetricsService metricsService;

    @Autowired
    public MetricsControllerImpl(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public ResponseEntity<ProgressResponse> getProgress(User user, UUID testId) {
        return ResponseEntity.ok(metricsService.getProgress(user, testId));
    }

    @Override
    public ResponseEntity<List<TagAccuracyDto>> getTagStats(User user, UUID testId) {
        return ResponseEntity.ok(metricsService.getTagStats(user, testId));
    }

    @Override
    public ResponseEntity<List<RankingEntryDto>> getRanking(UUID testId, String sortBy) {
        return ResponseEntity.ok(metricsService.getRanking(testId, sortBy));
    }
}
