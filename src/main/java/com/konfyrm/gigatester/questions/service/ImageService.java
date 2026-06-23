package com.konfyrm.gigatester.questions.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImageService {

    String upload(UUID questionId, MultipartFile file) throws IOException;

    List<String> listImages(UUID questionId);

    void delete(UUID questionId, String imageId);

    void deleteAll(UUID questionId);

    String uploadWithKey(String key, MultipartFile file) throws IOException;

}
