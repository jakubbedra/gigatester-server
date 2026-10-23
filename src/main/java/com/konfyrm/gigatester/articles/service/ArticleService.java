package com.konfyrm.gigatester.articles.service;

import com.konfyrm.gigatester.articles.domain.dto.request.ArticleRequest;
import com.konfyrm.gigatester.articles.domain.dto.response.ArticleResponse;
import com.konfyrm.gigatester.articles.domain.dto.response.ArticleSummaryResponse;
import com.konfyrm.gigatester.articles.domain.entity.Article;
import com.konfyrm.gigatester.articles.repository.ArticleRepository;
import com.konfyrm.gigatester.questions.service.ImageService;
import com.konfyrm.gigatester.questions.service.impl.R2ImageService;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ArticleService {

    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;

    private final ArticleRepository articleRepository;
    private final SubjectRepository subjectRepository;
    private final TestRepository testRepository;
    private final ImageService imageService;

    public ArticleService(ArticleRepository articleRepository,
                          SubjectRepository subjectRepository,
                          TestRepository testRepository,
                          @Qualifier(R2ImageService.QUALIFIER) ImageService imageService) {
        this.articleRepository = articleRepository;
        this.subjectRepository = subjectRepository;
        this.testRepository = testRepository;
        this.imageService = imageService;
    }

    public Article find(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found: " + id));
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional
    public Article add(ArticleRequest request, User creator) {
        Article article = Article.builder().createdBy(creator).createdAt(Instant.now()).build();
        apply(article, request);
        return articleRepository.save(article);
    }

    @Transactional
    public Article update(UUID id, ArticleRequest request) {
        Article article = find(id);
        apply(article, request);
        return articleRepository.save(article);
    }

    @Transactional
    public void delete(UUID id) {
        subjectRepository.findByArticles_Id(id).forEach(s -> s.getArticles().removeIf(a -> a.getId().equals(id)));
        articleRepository.deleteById(id);
    }

    private void apply(Article article, ArticleRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An article needs a title.");
        }
        article.setName(request.getName().trim());
        article.setContent(request.getContent());
        article.setPublished(request.isPublished());
        article.setRelatedTestId(request.getRelatedTestId());
        article.setUpdatedAt(Instant.now());
    }

    /** Stores an image for an article and returns its public URL. */
    public String uploadImage(UUID articleId, MultipartFile file) throws IOException {
        find(articleId);
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images can be uploaded.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image is larger than 8 MB.");
        }
        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String safeName = original.replaceAll("[^A-Za-z0-9._-]", "_");
        return imageService.uploadWithKey("articles/" + articleId + "/" + UUID.randomUUID() + "-" + safeName, file);
    }

    public ArticleResponse toResponse(Article article, boolean canEdit) {
        String testName = article.getRelatedTestId() == null ? null
                : testRepository.findById(article.getRelatedTestId())
                        .map(com.konfyrm.gigatester.tests.domain.entity.Test::getName).orElse(null);
        return ArticleResponse.builder()
                .id(article.getId())
                .name(article.getName())
                .content(article.getContent())
                .published(article.isPublished())
                .relatedTestId(testName == null ? null : article.getRelatedTestId())
                .relatedTestName(testName)
                .updatedAt(article.getUpdatedAt())
                .authorName(article.getCreatedBy() == null ? null : article.getCreatedBy().getUsername())
                .canEdit(canEdit)
                .build();
    }

    public ArticleSummaryResponse toSummary(Article article) {
        return ArticleSummaryResponse.builder()
                .id(article.getId())
                .name(article.getName())
                .published(article.isPublished())
                .build();
    }

}
