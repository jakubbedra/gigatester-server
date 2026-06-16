package com.konfyrm.gigatester.tests.service.checker;

import com.konfyrm.gigatester.questions.domain.converter.impl.TermDefinitionPairConverter;
import com.konfyrm.gigatester.questions.domain.entity.TermDefinitionPair;
import com.konfyrm.gigatester.questions.domain.entity.TermDefinitionQuestion;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.request.TermDefinitionQuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.TermDefinitionQuestionState;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TermDefinitionQuestionChecker implements QuestionChecker {

    private final TermDefinitionPairConverter termDefinitionPairConverter;

    public TermDefinitionQuestionChecker() {
        this.termDefinitionPairConverter = TermDefinitionPairConverter.INSTANCE;
    }

    @Nonnull
    @Override
    public QuestionState check(@Nonnull QuestionState state, @Nonnull QuestionStateRequest request) {
        if (!(state instanceof TermDefinitionQuestionState termDefinitionQuestionState)) {
            throw new IllegalArgumentException("TermDefinitionQuestionChecker called for invalid state class: " + state.getClass());
        }
        if (!(request instanceof TermDefinitionQuestionStateRequest termDefinitionQuestionStateRequest)) {
            throw new IllegalArgumentException("TermDefinitionQuestionChecker called for invalid state request class: " + request.getClass());
        }
        if (!(state.getQuestion() instanceof TermDefinitionQuestion termDefinitionQuestion)) {
            throw new IllegalArgumentException("TermDefinitionQuestionChecker called for invalid question class: " + state.getQuestion().getClass());
        }
        state.setAnswered(true);
        List<TermDefinitionPair> termDefinitions = termDefinitionQuestion.getTermDefinitions();
        List<TermDefinitionPair> newTermDefinitions = termDefinitionQuestionStateRequest.getTermDefinitions().stream()
                .map(termDefinitionPairConverter::toEntity)
                .collect(Collectors.toCollection(ArrayList::new));
        termDefinitionQuestionState.setTermDefinitions(newTermDefinitions);
        for (TermDefinitionPair termDefinition : termDefinitions) {
            boolean isCorrect = newTermDefinitions.stream()
                    .filter(dto -> dto.getTerm().equals(termDefinition.getTerm()))
                    .findFirst()
                    .map(dto -> dto.getDefinitions().containsAll(termDefinition.getDefinitions()) && termDefinition.getDefinitions().containsAll(dto.getDefinitions()))
                    .orElse(false);
            if (!isCorrect) {
                state.setScore(0.0);
                state.setWasCorrectAnswer(false);
                return state;
            }
        }
        state.setScore(termDefinitionQuestion.getPoints());
        state.setWasCorrectAnswer(true);
        return state;
    }

}
