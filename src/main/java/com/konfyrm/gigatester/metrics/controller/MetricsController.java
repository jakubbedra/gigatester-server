package com.konfyrm.gigatester.metrics.controller;

import com.konfyrm.gigatester.metrics.domain.dto.ProgressResponse;
import com.konfyrm.gigatester.metrics.domain.dto.RankingEntryDto;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("api/v1/metrics")
public interface MetricsController {

    @GetMapping("/progress")
    ResponseEntity<ProgressResponse> getProgress(@AuthenticationPrincipal User user);

    @GetMapping("/ranking")
    ResponseEntity<List<RankingEntryDto>> getRanking();
}
