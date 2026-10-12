package com.konfyrm.gigatester.coding.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One exercise inside a {@link CodingTask}: its own statement, starter code, tests and points. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coding_problems")
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

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

    @ElementCollection
    @CollectionTable(name = "coding_problem_tests", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    private List<CodingTestCase> tests = new ArrayList<>();

}
