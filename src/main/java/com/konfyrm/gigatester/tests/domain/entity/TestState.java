package com.konfyrm.gigatester.tests.domain.entity;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_states")
public class TestState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Test test;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private TesterEntityType type = TesterEntityType.TEST_STATE;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<QuestionState> questions;

    private int closedQuestionsCount;

    private int openQuestionsCount;

    @Builder.Default
    private Integer statementQuestionsCount = 0;

    @Setter
    @Builder.Default
    private int currentQuestionIndex = 0;

    @Nullable
    private Double passingPercentage;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TestExecutionState executionState = TestExecutionState.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    private TestMode mode;

    @Enumerated(EnumType.STRING)
    private TestDisplayType displayType;

    private boolean timeLimitEnabled = false;

    @Column(columnDefinition = "bigint default -1")
    private long timeLimitMs = -1;

    @Column(columnDefinition = "bigint default 0")
    private long startTime;

    /**
     * LEARNING mode retries wrong questions in rounds, discarding each round's
     * QuestionStates (orphanRemoval on `questions`) once it's superseded — so by
     * the time the test finishes, `questions` only holds the last, all-correct
     * round. These two counters accumulate every round's outcome as it happens,
     * so final stats reflect every attempt, not just the final round. Boxed and
     * left nullable so rows from before this column existed don't crash; treat
     * null as 0 wherever read.
     */
    @Builder.Default
    private Integer cumulativeAttempted = 0;

    @Builder.Default
    private Integer cumulativeCorrect = 0;

}