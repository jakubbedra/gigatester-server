package com.konfyrm.gigatester.tests.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_templates")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @ManyToMany
    private List<Question> questions;

    private int closedQuestionsCount;

    private int openQuestionsCount;

    private double passingPercentage;

    @Column(columnDefinition = "bigint default 0")
    private long timeLimit;

    @ManyToMany
    @JoinTable(name = "test_authors",
        joinColumns = @JoinColumn(name = "test_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<User> authors = new ArrayList<>();

}