package com.konfyrm.gigatester.questions.domain.entity;

import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A user-submitted report flagging a problem with a question, sent to every
 * author of the test it was encountered in. Shows up in those authors' (and
 * admins') inbox until resolved.
 */
@Entity
@Table(name = "question_reports")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class QuestionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    /** Null when the report was submitted anonymously. */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id")
    private User reportedBy;

    @Builder.Default
    private boolean anonymous = false;

    @Column(columnDefinition = "text")
    private String message;

    @Builder.Default
    private boolean resolved = false;

    private LocalDateTime createdAt;

}
