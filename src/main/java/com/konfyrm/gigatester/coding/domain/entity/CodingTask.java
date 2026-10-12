package com.konfyrm.gigatester.coding.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A "write a Python program" exercise, like on classic coding sites: a description, starter code,
 * and a list of stdin/stdout test cases. The score is the share of passed tests times {@link #points}.
 */
@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coding_tasks")
public class CodingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    /** HTML written in the rich-text editor. */
    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String description;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String starterCode;

    @Builder.Default
    private double points = 10;

    /** How long one test run may take in the student's browser before it counts as failed. */
    @Builder.Default
    private long timeLimitMs = 5000;

    /** Optional schedule: when set, students can only open and submit inside [startAt, windowEnd]. */
    private Instant startAt;

    private Instant windowEnd;

    public boolean isScheduled() {
        return startAt != null && windowEnd != null;
    }

    /** The exercises of this programming test, in order. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "task_id")
    @OrderColumn(name = "position")
    @Builder.Default
    private List<CodingProblem> problems = new ArrayList<>();

    // ── Legacy: before a task could hold several problems, description/starter code/points/tests lived
    // here. They are only read once, to turn such a task into a one-problem task (see CodingTaskService). ──

    @ElementCollection
    @CollectionTable(name = "coding_task_tests", joinColumns = @JoinColumn(name = "task_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    private List<CodingTestCase> tests = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

}
