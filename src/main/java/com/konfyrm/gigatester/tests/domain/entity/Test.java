package com.konfyrm.gigatester.tests.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_templates")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @ManyToMany
    private List<Question> questions;

    private int closedQuestionsCount;

    private int openQuestionsCount;

    private double passingPercentage;

    @Column(columnDefinition = "bigint default 0")
    private long timeLimit;

    @Builder.Default
    private Boolean hideTagsDuringTest = false;

    /** Null on tests created before types existed — treat as STANDARD via effectiveType(). */
    @Enumerated(EnumType.STRING)
    private TestType type;

    /** SCHEDULED_EXAM only: when taking the exam opens. */
    @Nullable
    private Instant startAt;

    /** SCHEDULED_EXAM only: last moment an attempt may be started; also the hard cutoff for answers. */
    @Nullable
    private Instant windowEnd;

    @ManyToMany
    @JoinTable(name = "test_authors",
        joinColumns = @JoinColumn(name = "test_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<User> authors = new ArrayList<>();

    public TestType effectiveType() {
        return type == null ? TestType.STANDARD : type;
    }

    public boolean isScheduledExam() {
        return effectiveType() == TestType.SCHEDULED_EXAM;
    }

    /**
     * Set once at creation. Used to authorize writes on tests not yet attached
     * to any subject (and therefore not yet resolvable to a subject group).
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

}