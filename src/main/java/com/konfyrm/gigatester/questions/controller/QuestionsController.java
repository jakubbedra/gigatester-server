package com.konfyrm.gigatester.questions.controller;

import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionsListDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface QuestionsController {

    @PostMapping("api/v1/questions")
    ResponseEntity<?> addQuestion(@RequestBody QuestionDto request);

    @PostMapping("api/v1/questions/bulk")
    ResponseEntity<?> addQuestions(@RequestBody QuestionsListDto request);

    @GetMapping("api/v1/questions")
    ResponseEntity<?> getQuestions();

    @GetMapping("api/v1/questions/{id}")
    ResponseEntity<?> getQuestion(@PathVariable("id") UUID id);

    @PutMapping("api/v1/questions/{id}")
    ResponseEntity<?> updateQuestion(@PathVariable("id") UUID id, @RequestBody QuestionDto request);

    @DeleteMapping("api/v1/questions/{id}")
    ResponseEntity<?> deleteQuestion(@PathVariable("id") UUID id);

}