package com.konfyrm.gigatester.questions.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.enums.MultipleChoiceScoringMode;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
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
public class ClosedQuestion extends Question {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClosedQuestionAnswer> answers;

    @Builder.Default
    private boolean multipleChoice = false;

    @Nullable
    private Double points;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MultipleChoiceScoringMode scoringMode = MultipleChoiceScoringMode.UNDEFINED;

}
