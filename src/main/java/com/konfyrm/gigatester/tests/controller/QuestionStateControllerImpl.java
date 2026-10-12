package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.tests.domain.converter.QuestionStatesConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.service.QuestionStateService;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.tests.service.ExamSupport;
import com.konfyrm.gigatester.tests.service.TestStateService;
import com.konfyrm.gigatester.users.domain.entity.User;
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
    public ResponseEntity<?> getQuestionState(UUID testStateId, UUID stateId, User user) {
        testStateService.assertCanReadAnswers(testStateId, stateId, user);
        QuestionState questionState = questionStateService.getQuestionState(stateId);
        QuestionStateResponse response = questionStatesConverter.toResponse(questionState);
        // During a scheduled exam the per-question score would reveal whether an answer was right.
        TestState testState = testStateService.findTestStateForOwnerOrGrader(testStateId, user);
        if (testState.getTest().isScheduledExam()
                && !ExamSupport.reviewAvailable(testState.getTest())
                && !testStateService.canGrade(user, testState.getTest().getId())) {
            response.setScore(0.0);
        }
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> updateQuestionState(UUID testStateId, UUID stateId, QuestionStateRequest request, User user) {
        testStateService.assertCanAnswer(testStateId, stateId, user);
        QuestionState answerQuestionState = questionStateService.checkQuestion(request, stateId);

        questionStateService.saveQuestionState(answerQuestionState);
        testStateService.recordLearningQuestionAttempt(testStateId, answerQuestionState);

        return ResponseEntity.noContent().build();
    }

}