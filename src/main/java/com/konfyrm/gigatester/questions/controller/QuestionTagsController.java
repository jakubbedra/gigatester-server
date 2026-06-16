package com.konfyrm.gigatester.questions.controller;

import com.konfyrm.gigatester.questions.domain.dto.QuestionTagDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface QuestionTagsController {

    @PostMapping("api/v1/questions/tags")
    ResponseEntity<?> addQuestionTag(@RequestBody QuestionTagDto request);

    @GetMapping("api/v1/questions/tags")
    ResponseEntity<?> getQuestionTags();

    @GetMapping("api/v1/questions/tags/{id}")
    ResponseEntity<?> getQuestionTag(@PathVariable("id") UUID id);

    @PutMapping("api/v1/questions/tags/{id}")
    ResponseEntity<?> updateQuestionTag(@PathVariable("id") UUID id, @RequestBody QuestionTagDto request);

    @DeleteMapping("api/v1/questions/tags/{id}")
    ResponseEntity<?> deleteQuestionTag(@PathVariable("id") UUID id);

}
