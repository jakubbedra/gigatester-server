package com.konfyrm.gigatester.ai.controller;

import com.konfyrm.gigatester.ai.dto.AiQuestionDto;
import com.konfyrm.gigatester.ai.dto.AiSaveRequest;
import com.konfyrm.gigatester.ai.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class AiControllerImpl implements AiController {

    private final AiService aiService;

    public AiControllerImpl(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    @PostMapping(value = "/generate-questions", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<List<AiQuestionDto>> generateQuestions(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "0") int closedCount,
            @RequestParam(defaultValue = "0") int multipleChoiceCount,
            @RequestParam(defaultValue = "0") int openCount
    ) {
        try {
            List<AiQuestionDto> questions = aiService.generateQuestions(file, closedCount, multipleChoiceCount, openCount);
            return ResponseEntity.ok(questions);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read PDF: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Generation failed: " + e.getMessage());
        }
    }

    @Override
    @PostMapping("/save-questions")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Void> saveQuestions(@RequestBody AiSaveRequest request) {
        aiService.saveQuestions(request.getTestId(), request.getQuestions());
        return ResponseEntity.ok().build();
    }
}
