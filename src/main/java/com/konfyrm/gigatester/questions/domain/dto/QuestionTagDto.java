package com.konfyrm.gigatester.questions.domain.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTagDto {

    private UUID id;

    private String value;

}