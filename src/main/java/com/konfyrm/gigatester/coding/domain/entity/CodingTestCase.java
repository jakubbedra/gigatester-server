package com.konfyrm.gigatester.coding.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

/** One check of a coding task: feed {@code input} to the program's stdin, expect {@code expectedOutput} on stdout. */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTestCase {

    @jakarta.persistence.Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String input;

    @jakarta.persistence.Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String expectedOutput;

    /** Hidden tests never reveal their expected output to students; they only see pass/fail. */
    private boolean hidden;

}
