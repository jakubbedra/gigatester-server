package com.konfyrm.gigatester.questions.domain.dto;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosedQuestionAnswerDto {

    @Nullable
    private UUID id;

    @Nonnull
    private String text;

    @Nonnull
    private Boolean correct;

    @Nullable
    private Double points;

}