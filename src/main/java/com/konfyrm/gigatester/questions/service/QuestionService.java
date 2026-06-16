package com.konfyrm.gigatester.questions.service;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.entity.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionService {

    List<Question> findQuestions();

    List<Question> findQuestions(UUID testId);

    List<Question> findQuestions(UUID testId, TesterEntityType type);

    Optional<Question> findQuestion(UUID questionId);

    Question saveQuestion(Question question);

    void deleteQuestion(UUID questionId);

}
