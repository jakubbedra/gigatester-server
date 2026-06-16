package com.konfyrm.gigatester.questions.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StatementQuestion extends Question {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Statement> statements;

    @Builder.Default
    private Double points = 1.0;

}
