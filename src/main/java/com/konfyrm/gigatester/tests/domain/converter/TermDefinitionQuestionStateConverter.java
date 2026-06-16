package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.questions.domain.converter.impl.TermDefinitionPairConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.TermDefinitionQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.TermDefinitionQuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.TermDefinitionQuestionState;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component(TermDefinitionQuestionStateConverter.QUALIFIER)
public class TermDefinitionQuestionStateConverter implements QuestionStateConverter {

    public static final String QUALIFIER = "termDefinitionQuestionStateConverter";

    private final TermDefinitionPairConverter termDefinitionPairConverter;

    public TermDefinitionQuestionStateConverter() {
        this.termDefinitionPairConverter = TermDefinitionPairConverter.INSTANCE;
    }

    @Nonnull
    @Override
    public QuestionStateResponse.QuestionStateResponseBuilder<?, ?> toResponseBuilder(@Nonnull QuestionState entity) {
        if (entity instanceof TermDefinitionQuestionState termDefinitionQuestionState) {
            return TermDefinitionQuestionStateResponse.builder()
                    .termDefinitions(termDefinitionQuestionState.getTermDefinitions().stream().map(termDefinitionPairConverter::toDto).toList());
        }
        throw new IllegalStateException("OpenQuestionStateConverter called for invalid class: " + entity.getClass().getName());
    }

    @Nonnull
    @Override
    public QuestionState.QuestionStateBuilder<?, ?> toEntityBuilder(@Nonnull QuestionStateRequest request) {
        if (request instanceof TermDefinitionQuestionStateRequest termDefinitionQuestionStateRequest) {
            return TermDefinitionQuestionState.builder()
                    .termDefinitions(termDefinitionQuestionStateRequest.getTermDefinitions().stream().map(termDefinitionPairConverter::toEntity).toList());
        }
        throw new IllegalStateException("OpenQuestionStateConverter called for invalid class: " + request.getClass().getName());
    }

}