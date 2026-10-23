package com.konfyrm.gigatester.articles.controller;

import com.konfyrm.gigatester.articles.domain.dto.request.ArticleRequest;
import com.konfyrm.gigatester.articles.domain.entity.Article;
import com.konfyrm.gigatester.articles.service.ArticleService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@RestController
public class ArticleControllerImpl implements ArticleController {

    private final ArticleService articleService;
    private final PermissionService permissionService;
    private final SubjectGroupAccessService accessService;

    public ArticleControllerImpl(ArticleService articleService, PermissionService permissionService,
                                 SubjectGroupAccessService accessService) {
        this.articleService = articleService;
        this.permissionService = permissionService;
        this.accessService = accessService;
    }

    @Override
    public ResponseEntity<?> addArticle(ArticleRequest request, User user) {
        permissionService.require(permissionService.canCreate(user, Permission.ARTICLES_WRITE));
        Article saved = articleService.add(request, user);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getArticles(User user) {
        permissionService.require(permissionService.isStaff(user));
        return ResponseEntity.ok(articleService.findAll().stream().map(articleService::toSummary).toList());
    }

    @Override
    public ResponseEntity<?> getReadableArticles(User user) {
        return ResponseEntity.ok(articleService.findAll().stream()
                .filter(a -> accessService.hasAccessToArticle(a.getId(), user))
                .filter(a -> a.isPublished() || permissionService.hasArticlePermission(user, a.getId(), Permission.ARTICLES_WRITE))
                .map(articleService::toSummary)
                .toList());
    }

    @Override
    public ResponseEntity<?> getArticle(UUID id, User user) {
        Article article = articleService.find(id);
        if (!accessService.hasAccessToArticle(id, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        boolean canEdit = permissionService.hasArticlePermission(user, id, Permission.ARTICLES_WRITE);
        if (!article.isPublished() && !canEdit) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found");
        }
        return ResponseEntity.ok(articleService.toResponse(article, canEdit));
    }

    @Override
    public ResponseEntity<?> updateArticle(UUID id, ArticleRequest request, User user) {
        permissionService.require(permissionService.hasArticlePermission(user, id, Permission.ARTICLES_WRITE));
        articleService.update(id, request);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteArticle(UUID id, User user) {
        permissionService.require(permissionService.hasArticlePermission(user, id, Permission.ARTICLES_WRITE));
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> uploadImage(UUID id, MultipartFile file, User user) {
        permissionService.require(permissionService.hasArticlePermission(user, id, Permission.ARTICLES_WRITE));
        try {
            return ResponseEntity.accepted().body(articleService.uploadImage(id, file));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }

}
