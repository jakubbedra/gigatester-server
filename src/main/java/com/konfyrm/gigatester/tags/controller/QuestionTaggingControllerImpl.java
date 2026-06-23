package com.konfyrm.gigatester.tags.controller;

import com.konfyrm.gigatester.tags.service.QuestionTaggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class QuestionTaggingControllerImpl implements QuestionTaggingController {

    private final QuestionTaggingService questionTaggingService;

    @Autowired
    public QuestionTaggingControllerImpl(QuestionTaggingService questionTaggingService) {
        this.questionTaggingService = questionTaggingService;
    }

    @Override
    public ResponseEntity<?> addTagToQuestion(UUID questionId, UUID tagId) {
        questionTaggingService.addTag(questionId, tagId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> removeTagFromQuestion(UUID questionId, UUID tagId) {
        questionTaggingService.removeTag(questionId, tagId);
        return ResponseEntity.noContent().build();
    }

}
