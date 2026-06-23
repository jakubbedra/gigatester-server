package com.konfyrm.gigatester.questions.service.impl;

import com.konfyrm.gigatester.questions.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service(R2ImageService.QUALIFIER)
public class R2ImageService implements ImageService {

    public static final String QUALIFIER = "imageServiceImpl";

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public R2ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(UUID questionId, MultipartFile file) throws IOException {
        String key = buildKey(questionId, file.getOriginalFilename());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return publicUrl + "/" + key;
    }

    @Override
    public List<String> listImages(UUID questionId) {
        String prefix = "questions/" + questionId + "/";

        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .build()
        );

        return response.contents().stream()
                .map(s3Object -> publicUrl + "/" + s3Object.key())
                .toList();
    }

    @Override
    public void delete(UUID questionId, String imageId) {
        String key = buildKey(questionId, imageId);

        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + imageId);
        }

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    @Override
    public void deleteAll(UUID questionId) {
        listImages(questionId).forEach(url -> {
            String key = url.replace(publicUrl + "/", "");
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        });
    }

    @Override
    public String uploadWithKey(String key, MultipartFile file) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        return publicUrl + "/" + key;
    }

    private String buildKey(UUID questionId, String filename) {
        return "questions/" + questionId + "/" + filename;
    }

}