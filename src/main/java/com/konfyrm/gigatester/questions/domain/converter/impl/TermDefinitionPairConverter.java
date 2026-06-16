package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import com.konfyrm.gigatester.questions.domain.dto.TermDefinitionPairDto;
import com.konfyrm.gigatester.questions.domain.entity.TermDefinitionPair;
import jakarta.annotation.Nonnull;

public enum TermDefinitionPairConverter implements EntityConverter<TermDefinitionPairDto, TermDefinitionPair> {
    INSTANCE;

    @Nonnull
    @Override
    public TermDefinitionPair toEntity(@Nonnull TermDefinitionPairDto termDefinitionPairDto) {
        return TermDefinitionPair.builder()
                .order(termDefinitionPairDto.getOrder())
                .term(termDefinitionPairDto.getTerm())
                .definitions(termDefinitionPairDto.getDefinitions())
                .build();
    }

    @Nonnull
    @Override
    public TermDefinitionPairDto toDto(@Nonnull TermDefinitionPair termDefinitionPair) {
        return TermDefinitionPairDto.builder()
                .order(termDefinitionPair.getOrder())
                .term(termDefinitionPair.getTerm())
                .definitions(termDefinitionPair.getDefinitions())
                .build();
    }

}