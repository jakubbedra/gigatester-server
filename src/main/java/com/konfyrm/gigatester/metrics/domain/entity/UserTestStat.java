package com.konfyrm.gigatester.metrics.domain.entity;

import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_test_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTestStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false)
    private boolean passed;

    @Column(nullable = false)
    private double scorePercent;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int correctQuestions;

    @Column(nullable = false)
    private LocalDate completedDate;

    @Column(nullable = false)
    private LocalDateTime completedAt;

}
