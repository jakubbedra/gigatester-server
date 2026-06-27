package com.konfyrm.gigatester.metrics.service;

import com.konfyrm.gigatester.metrics.domain.entity.DailyStreak;
import com.konfyrm.gigatester.metrics.repository.DailyStreakRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DailyStreakService {

    private final DailyStreakRepository repository;

    @Autowired
    public DailyStreakService(DailyStreakRepository repository) {
        this.repository = repository;
    }

    public void recordActivity(User user) {
        LocalDate today = LocalDate.now();
        DailyStreak streak = repository.findByUserId(user.getId())
                .orElseGet(() -> DailyStreak.builder()
                        .user(user)
                        .currentStreak(0)
                        .longestStreak(0)
                        .lastActivityDate(today.minusDays(2))
                        .build());

        if (today.equals(streak.getLastActivityDate())) {
            return; // already counted today
        }

        if (today.minusDays(1).equals(streak.getLastActivityDate())) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streak.setLastActivityDate(today);
        repository.save(streak);
    }

    public DailyStreak getStreak(User user) {
        return repository.findByUserId(user.getId())
                .orElseGet(() -> DailyStreak.builder()
                        .user(user)
                        .currentStreak(0)
                        .longestStreak(0)
                        .lastActivityDate(null)
                        .build());
    }

}
