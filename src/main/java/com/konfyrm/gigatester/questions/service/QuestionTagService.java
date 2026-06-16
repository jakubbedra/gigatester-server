package com.konfyrm.gigatester.questions.service;

import com.konfyrm.gigatester.questions.domain.entity.QuestionTag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionTagService {

    List<QuestionTag> findAll();

    Optional<QuestionTag> find(UUID tagId);

    QuestionTag save(QuestionTag tag);

    void delete(UUID tagId);

}
