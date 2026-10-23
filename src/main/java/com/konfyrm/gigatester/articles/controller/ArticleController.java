package com.konfyrm.gigatester.articles.controller;

import com.konfyrm.gigatester.articles.domain.dto.request.ArticleRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ArticleController {

    @PostMapping("api/v1/articles")
    ResponseEntity<?> addArticle(@RequestBody ArticleRequest request, @AuthenticationPrincipal User user);

    /** Staff overview, drafts included. */
    @GetMapping("api/v1/articles")
    ResponseEntity<?> getArticles(@AuthenticationPrincipal User user);

    /** What this user may read: published articles from their groups (staff also see drafts). */
    @GetMapping("api/v1/articles/readable")
    ResponseEntity<?> getReadableArticles(@AuthenticationPrincipal User user);

    @GetMapping("api/v1/articles/{id}")
    ResponseEntity<?> getArticle(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/articles/{id}")
    ResponseEntity<?> updateArticle(@PathVariable("id") UUID id, @RequestBody ArticleRequest request,
                                    @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/articles/{id}")
    ResponseEntity<?> deleteArticle(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

    @PostMapping("api/v1/articles/{id}/images")
    ResponseEntity<?> uploadImage(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file,
                                  @AuthenticationPrincipal User user);

}
