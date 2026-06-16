package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.converter.QuestionStatesConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.service.QuestionStateService;
import com.konfyrm.gigatester.tests.service.TestStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class QuestionStateControllerImpl implements QuestionStateController {

    private final QuestionStateService questionStateService;
    private final TestStateService testStateService;

    private final QuestionStatesConverter questionStatesConverter;

    @Autowired
    public QuestionStateControllerImpl(
            QuestionStateService questionStateService,
            TestStateService testStateService,
            QuestionStatesConverter questionStatesConverter
    ) {
        this.questionStateService = questionStateService;
        this.testStateService = testStateService;
        this.questionStatesConverter = questionStatesConverter;
    }

    @Override
    public ResponseEntity<?> getQuestionState(UUID testStateId, UUID stateId) {
        QuestionState questionState = questionStateService.getQuestionState(stateId);
        return ResponseEntity.ok(questionStatesConverter.toResponse(questionState));
    }
//todo: also a bulk-endpoint for all at once mode (not yet available, focus on learning + one-by-one mode currently)
    @Override
    public ResponseEntity<?> updateQuestionState(UUID testStateId, UUID stateId, QuestionStateRequest request) {
        QuestionState answerQuestionState = questionStateService.checkQuestion(request, stateId);

        questionStateService.saveQuestionState(answerQuestionState);

        return ResponseEntity.noContent().build();
    }

}