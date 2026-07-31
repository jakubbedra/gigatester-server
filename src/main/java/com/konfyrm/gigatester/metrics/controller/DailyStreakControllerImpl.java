package com.konfyrm.gigatester.metrics.controller;

import com.konfyrm.gigatester.metrics.domain.entity.DailyStreak;
import com.konfyrm.gigatester.metrics.service.DailyStreakService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DailyStreakControllerImpl implements DailyStreakController {

    private final DailyStreakService streakService;

    @Autowired
    public DailyStreakControllerImpl(DailyStreakService streakService) {
        this.streakService = streakService;
    }

    @Override
    public ResponseEntity<Map<String, Object>> getStreak(User user) {
        DailyStreak streak = streakService.getStreak(user);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("currentStreak", streak.getCurrentStreak());
        body.put("longestStreak", streak.getLongestStreak());
        body.put("lastActivityDate", streak.getLastActivityDate());
        return ResponseEntity.ok(body);
    }

}
