package com.konfyrm.gigatester.metrics.controller;

import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/api/v1/streak")
public interface DailyStreakController {

    @GetMapping
    ResponseEntity<Map<String, Object>> getStreak(@AuthenticationPrincipal User user);

}
