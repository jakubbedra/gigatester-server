package com.konfyrm.gigatester.questions.controller.impl;

import com.konfyrm.gigatester.questions.controller.ImagesController;
import com.konfyrm.gigatester.questions.service.ImageService;
import com.konfyrm.gigatester.questions.service.impl.R2ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
public class QuestionImagesControllerImpl implements ImagesController {

    private final ImageService imageService;

    @Autowired
    public QuestionImagesControllerImpl(
            @Qualifier((R2ImageService.QUALIFIER)) ImageService imageService
    ) {
        this.imageService = imageService;
    }

    @Override
    public ResponseEntity<?> uploadImage(UUID questionId, MultipartFile file) {
        try {
            String imageUrl = imageService.upload(questionId, file);
            return ResponseEntity.accepted().body(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getImages(UUID questionId) {
        return ResponseEntity.ok(imageService.listImages(questionId));
    }

    @Override
    public ResponseEntity<?> deleteImage(UUID questionId, String imageId) {
        imageService.delete(questionId, imageId);
        return ResponseEntity.noContent().build();
    }

}