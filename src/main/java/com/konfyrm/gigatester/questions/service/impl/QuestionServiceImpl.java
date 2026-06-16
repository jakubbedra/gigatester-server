package com.konfyrm.gigatester.questions.service.impl;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.questions.service.QuestionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Transactional
@Service(QuestionServiceImpl.QUALIFIER)
public class QuestionServiceImpl implements QuestionService {

    public static final String QUALIFIER = "questionServiceImpl";

    private static final Set<TesterEntityType> SUPPORTED_TYPES = Set.of(
            TesterEntityType.OPEN_QUESTION, TesterEntityType.CLOSED_QUESTION, TesterEntityType.TERM_DEFINITION_QUESTION
    );

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Question> findQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> findQuestions(UUID testId) {
        return questionRepository.findAllByTestId(testId);
    }

    @Override
    public List<Question> findQuestions(UUID testId, TesterEntityType type) {
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported tester entity type: " + type);
        }
        return questionRepository.findAllByTestId(testId);
    }

    @Override
    public Optional<Question> findQuestion(UUID questionId) {
        return questionRepository.findById(questionId);
    }

    @Override
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(UUID questionId) {
        questionRepository.deleteById(questionId);
    }

}