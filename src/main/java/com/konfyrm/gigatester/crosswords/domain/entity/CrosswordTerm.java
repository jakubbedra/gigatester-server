package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.crosswords.domain.entity.enums.ClueType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crossword_terms")
public class CrosswordTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String term;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String clue;

    private ClueType clueType;

}