package com.konfyrm.gigatester.metrics.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_streaks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int currentStreak;

    @Column(nullable = false)
    private int longestStreak;

    @Column(nullable = false)
    private LocalDate lastActivityDate;

}
