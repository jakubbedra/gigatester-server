package com.konfyrm.gigatester.tests.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_states")
public abstract class QuestionState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Question question;

    @Builder.Default
    private Double score = 0.0;

    @Builder.Default
    private boolean answered = false;

    @Builder.Default
    private boolean wasCorrectAnswer = false;

}