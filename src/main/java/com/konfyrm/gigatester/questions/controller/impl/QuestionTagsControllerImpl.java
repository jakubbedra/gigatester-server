package com.konfyrm.gigatester.questions.controller.impl;

import com.konfyrm.gigatester.questions.controller.QuestionTagsController;
import com.konfyrm.gigatester.questions.domain.converter.impl.QuestionTagConverter;
import com.konfyrm.gigatester.questions.domain.dto.QuestionTagDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionTagListDto;
import com.konfyrm.gigatester.questions.domain.entity.QuestionTag;
import com.konfyrm.gigatester.questions.service.QuestionTagService;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@RestController
public class QuestionTagsControllerImpl implements QuestionTagsController {

    private final QuestionTagService questionTagService;

    private final QuestionTagConverter questionTagConverter;

    @Autowired
    public QuestionTagsControllerImpl(
            @Nonnull QuestionTagService questionTagService,
            @Nonnull QuestionTagConverter questionTagConverter
    ) {
        this.questionTagService = questionTagService;
        this.questionTagConverter = questionTagConverter;
    }

    @Override
    public ResponseEntity<?> addQuestionTag(QuestionTagDto request) {
        QuestionTag saved = questionTagService.save(questionTagConverter.toEntity(request));
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getQuestionTags() {
        return ResponseEntity.ok(QuestionTagListDto.builder()
                .questionTags(questionTagService.findAll().stream().map(questionTagConverter::toDto).toList())
                .build());
    }

    @Override
    public ResponseEntity<?> getQuestionTag(UUID id) {
        return ResponseEntity.ok(questionTagService.find(id)
                .map(questionTagConverter::toDto)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND))
        );
    }

    @Override
    public ResponseEntity<?> updateQuestionTag(UUID id, QuestionTagDto request) {
        QuestionTag questionTag = questionTagService.find(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        questionTag.setValue(request.getValue());
        questionTagService.save(questionTag);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> deleteQuestionTag(UUID id) {
        questionTagService.delete(id);
        return ResponseEntity.noContent().build();
    }

}