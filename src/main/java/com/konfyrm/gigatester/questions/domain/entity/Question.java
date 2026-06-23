package com.konfyrm.gigatester.questions.domain.entity;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.tags.entity.Tag;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="questions")
public abstract class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TesterEntityType type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "content_id")
    private QuestionContent content;

    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "explanation_id")
    private QuestionContent explanation;

    @Builder.Default
    private Integer contentProportions = 1;

    @Builder.Default
    private Integer answerProportions = 1;

    @ManyToMany
    private List<Tag> tags = new ArrayList<>();

}