package com.konfyrm.gigatester.tests.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.TermDefinitionPair;
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
public class TermDefinitionQuestionState extends QuestionState {

    @ElementCollection
    @CollectionTable(
            name = "term_definition_states",
            joinColumns = @JoinColumn(name = "question_state_id")
    )
    @Builder.Default
    private List<TermDefinitionPair> termDefinitions = new ArrayList<>();

}