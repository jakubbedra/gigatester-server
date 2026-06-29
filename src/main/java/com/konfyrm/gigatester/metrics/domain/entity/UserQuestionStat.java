package com.konfyrm.gigatester.metrics.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "user_question_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestionStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private int timesAnswered;

    @Column(nullable = false)
    private int timesCorrect;

}
