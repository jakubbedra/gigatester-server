package com.konfyrm.gigatester.ai.controller;

import com.konfyrm.gigatester.ai.dto.AiQuestionDto;
import com.konfyrm.gigatester.ai.dto.AiSaveRequest;
import com.konfyrm.gigatester.ai.service.AiService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class AiControllerImpl implements AiController {

    private final AiService aiService;
    private final PermissionService permissionService;

    public AiControllerImpl(AiService aiService, PermissionService permissionService) {
        this.aiService = aiService;
        this.permissionService = permissionService;
    }

    @Override
    public ResponseEntity<List<AiQuestionDto>> generateQuestions(
            MultipartFile file,
            int closedCount,
            int multipleChoiceCount,
            int openCount,
            int answerCount,
            User user
    ) {
        permissionService.require(permissionService.canCreate(user, Permission.TESTS_WRITE));
        try {
            List<AiQuestionDto> questions = aiService.generateQuestions(file, closedCount, multipleChoiceCount, openCount, answerCount);
            return ResponseEntity.ok(questions);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read PDF: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Generation failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> saveQuestions(AiSaveRequest request, User user) {
        permissionService.require(permissionService.canCreate(user, Permission.TESTS_WRITE));
        aiService.saveQuestions(request.getTestId(), request.getQuestions());
        return ResponseEntity.ok().build();
    }

}