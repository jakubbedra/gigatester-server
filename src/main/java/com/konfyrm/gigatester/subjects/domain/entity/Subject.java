package com.konfyrm.gigatester.subjects.domain.entity;

import com.konfyrm.gigatester.comments.domain.entity.Comment;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "float8 default 0")
    private double difficulty;

    @ManyToMany
    private List<Test> tests;

    @ManyToMany
    private List<Crossword> crosswords;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

}