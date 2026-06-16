package com.konfyrm.gigatester.questions.domain.entity;

import com.konfyrm.gigatester.questions.domain.entity.enums.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="question_contents")
public class QuestionContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private ContentType type;

    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String text;

}