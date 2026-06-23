package com.konfyrm.gigatester.tags.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface QuestionTaggingController {

    @PutMapping("api/v1/questions/{questionId}/tags/{tagId}")
    ResponseEntity<?> addTagToQuestion(@PathVariable UUID questionId, @PathVariable UUID tagId);

    @DeleteMapping("api/v1/questions/{questionId}/tags/{tagId}")
    ResponseEntity<?> removeTagFromQuestion(@PathVariable UUID questionId, @PathVariable UUID tagId);

}
