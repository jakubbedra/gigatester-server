package com.konfyrm.gigatester.ai.controller;

import com.konfyrm.gigatester.ai.dto.AiQuestionDto;
import com.konfyrm.gigatester.ai.dto.AiSaveRequest;
import com.konfyrm.gigatester.ai.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Component
public class AiControllerImpl implements AiController {

    private final AiService aiService;

    public AiControllerImpl(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<List<AiQuestionDto>> generateQuestions(
            MultipartFile file,
            int closedCount,
            int multipleChoiceCount,
            int openCount
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
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Void> saveQuestions(AiSaveRequest request) {
        aiService.saveQuestions(request.getTestId(), request.getQuestions());
        return ResponseEntity.ok().build();
    }
}
