package com.konfyrm.gigatester.ai.controller;

import com.konfyrm.gigatester.ai.dto.AiQuestionDto;
import com.konfyrm.gigatester.ai.dto.AiSaveRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AiController {
    ResponseEntity<List<AiQuestionDto>> generateQuestions(
            MultipartFile file,
            int closedCount,
            int multipleChoiceCount,
            int openCount
    );

    ResponseEntity<Void> saveQuestions(AiSaveRequest request);
}
