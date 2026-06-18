package com.konfyrm.gigatester.questions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImagesController {

    @PostMapping("api/v1/questions/{questionId}/images")
    ResponseEntity<?> uploadImage(
            @PathVariable("questionId") UUID questionId,
            @RequestParam("file") MultipartFile file
    );

    @GetMapping("api/v1/questions/{questionId}/images")
    ResponseEntity<?> getImages(@PathVariable("questionId") UUID questionId);

    @DeleteMapping("api/v1/questions/{questionId}/images/{imageId}")
    ResponseEntity<?> deleteImage(
            @PathVariable("questionId") UUID questionId,
            @PathVariable("imageId") String imageId
    );

}
