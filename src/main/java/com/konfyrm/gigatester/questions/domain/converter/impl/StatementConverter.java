package com.konfyrm.gigatester.questions.domain.converter.impl;

import com.konfyrm.gigatester.questions.domain.converter.EntityConverter;
import com.konfyrm.gigatester.questions.domain.dto.StatementDto;
import com.konfyrm.gigatester.questions.domain.entity.Statement;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component
public class StatementConverter implements EntityConverter<StatementDto, Statement> {

    @Nonnull
    @Override
    public Statement toEntity(@Nonnull StatementDto statementDto) {
        return Statement.builder()
                .text(statementDto.getText())
                .answer(statementDto.isAnswer())
                .build();
    }

    @Nonnull
    @Override
    public StatementDto toDto(@Nonnull Statement statement) {
        return StatementDto.builder()
                .text(statement.getText())
                .answer(statement.isAnswer())
                .build();
    }

}