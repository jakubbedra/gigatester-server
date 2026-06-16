package com.konfyrm.gigatester.tests.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StatementQuestionState extends QuestionState {

    @ElementCollection
    @Builder.Default
    private List<Boolean> answers = new ArrayList<>();

}
