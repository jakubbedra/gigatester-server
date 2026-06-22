package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.tests.service.checker.*;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.repository.QuestionStateRepository;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class QuestionStateService {

    private static final Map<QuestionType, QuestionChecker> QUESTION_CHECKERS = Map.of(
            QuestionType.CLOSED, new ClosedQuestionChecker(),
            QuestionType.OPEN, new OpenQuestionChecker(),
            QuestionType.STATEMENT, new StatementQuestionChecker()
    );

    private static final Map<TesterEntityType, QuestionChecker> ENTITY_TYPE_TO_QUESTION_CHECKERS = Map.of(
            TesterEntityType.CLOSED_QUESTION, new ClosedQuestionChecker(),
            TesterEntityType.OPEN_QUESTION, new OpenQuestionChecker(),
            TesterEntityType.STATEMENT_QUESTION, new StatementQuestionChecker()
    );

    private final QuestionStateRepository questionStateRepository;

    @Autowired
    public QuestionStateService(QuestionStateRepository questionStateRepository) {
        this.questionStateRepository = questionStateRepository;
    }

    @Nonnull
    public QuestionState checkQuestion(@Nonnull QuestionStateRequest request, @Nonnull UUID stateId) {
        QuestionState questionState = getQuestionState(stateId);
        QuestionChecker questionChecker = QUESTION_CHECKERS.get(request.getQuestionType());
        if (questionChecker.hasConflict(questionState, request)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Question already answered");
        }
        return questionChecker.check(questionState, request);
    }

    @Nonnull
    public QuestionState checkQuestion(@Nonnull QuestionState questionState, boolean answered) {
        return ENTITY_TYPE_TO_QUESTION_CHECKERS.get(questionState.getQuestion().getType()).check(questionState, answered);
    }

    public QuestionState getQuestionState(UUID stateId) {
        return questionStateRepository.findById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question state with the following id not found: " + stateId));
    }

    public QuestionState saveQuestionState(QuestionState questionState) {
        return questionStateRepository.save(questionState);
    }

}
