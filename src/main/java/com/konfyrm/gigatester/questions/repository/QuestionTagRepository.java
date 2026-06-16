package com.konfyrm.gigatester.questions.repository;

import com.konfyrm.gigatester.questions.domain.entity.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, UUID> {
}
