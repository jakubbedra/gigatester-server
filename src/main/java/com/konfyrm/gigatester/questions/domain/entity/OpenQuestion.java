package com.konfyrm.gigatester.questions.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OpenQuestion extends Question {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "answer_id")
    private QuestionContent answer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QuestionContent> answerVariations = new HashSet<>();

    private String gradingRulesHash;

    private Double points;

}