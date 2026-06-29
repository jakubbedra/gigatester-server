package com.konfyrm.gigatester.metrics.controller;

import com.konfyrm.gigatester.metrics.domain.dto.ProgressResponse;
import com.konfyrm.gigatester.metrics.domain.dto.RankingEntryDto;
import com.konfyrm.gigatester.metrics.service.MetricsService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MetricsControllerImpl implements MetricsController {

    private final MetricsService metricsService;

    @Autowired
    public MetricsControllerImpl(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public ResponseEntity<ProgressResponse> getProgress(User user) {
        return ResponseEntity.ok(metricsService.getProgress(user));
    }

    @Override
    public ResponseEntity<List<RankingEntryDto>> getRanking() {
        return ResponseEntity.ok(metricsService.getRanking());
    }
}
