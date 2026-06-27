package com.konfyrm.gigatester.ai.controller;

import com.konfyrm.gigatester.ai.dto.AiQuestionDto;
import com.konfyrm.gigatester.ai.dto.AiSaveRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1/ai")
public interface AiController {

    @PostMapping(value = "/generate-questions", consumes = "multipart/form-data")
    ResponseEntity<List<AiQuestionDto>> generateQuestions(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "0") int closedCount,
            @RequestParam(defaultValue = "0") int multipleChoiceCount,
            @RequestParam(defaultValue = "0") int openCount
    );

    @PostMapping("/save-questions")
    ResponseEntity<Void> saveQuestions(@RequestBody AiSaveRequest request);
}
