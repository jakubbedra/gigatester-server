package com.konfyrm.gigatester.questions.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TermDefinitionQuestion extends Question {

    @ElementCollection
    @CollectionTable(
            name = "term_definitions",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Builder.Default
    private List<TermDefinitionPair> termDefinitions = new ArrayList<>();

    private Double points;

}