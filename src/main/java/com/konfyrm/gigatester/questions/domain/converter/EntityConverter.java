package com.konfyrm.gigatester.questions.domain.converter;

import jakarta.annotation.Nonnull;

public interface EntityConverter<DTO, ENTITY> {

    @Nonnull
    ENTITY toEntity(@Nonnull DTO dto);

    @Nonnull
    DTO toDto(@Nonnull ENTITY entity);

}
