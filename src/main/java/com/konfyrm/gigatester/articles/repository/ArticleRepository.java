package com.konfyrm.gigatester.articles.repository;

import com.konfyrm.gigatester.articles.domain.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> { }
