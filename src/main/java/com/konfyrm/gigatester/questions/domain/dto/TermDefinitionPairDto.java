package com.konfyrm.gigatester.questions.domain.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermDefinitionPairDto {

    private Integer order;

    private String term;

    private Set<String> definitions;

}