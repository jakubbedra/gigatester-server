package com.konfyrm.gigatester.metrics.controller;

import com.konfyrm.gigatester.metrics.domain.dto.ProgressResponse;
import com.konfyrm.gigatester.metrics.domain.dto.RankingEntryDto;
import com.konfyrm.gigatester.metrics.domain.dto.TagAccuracyDto;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RequestMapping("api/v1/metrics")
public interface MetricsController {

    @GetMapping("/progress")
    ResponseEntity<ProgressResponse> getProgress(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID testId);

    @GetMapping("/tag-stats")
    ResponseEntity<List<TagAccuracyDto>> getTagStats(
            @AuthenticationPrincipal User user,
            @RequestParam UUID testId);

    @GetMapping("/ranking")
    ResponseEntity<List<RankingEntryDto>> getRanking(
            @RequestParam(required = false) UUID testId,
            @RequestParam(required = false) String sortBy);
}
