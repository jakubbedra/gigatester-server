package com.konfyrm.gigatester.coding.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.UUID;

/** One graded attempt. The server compares the submitted outputs with the stored expectations itself. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coding_submissions")
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private CodingTask task;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private CodingProblem problem;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String code;

    private int passedCount;

    private int totalCount;

    private double score;

    private Instant submittedAt;

}
