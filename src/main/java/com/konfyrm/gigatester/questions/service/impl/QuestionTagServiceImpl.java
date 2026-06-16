package com.konfyrm.gigatester.questions.service.impl;

import com.konfyrm.gigatester.questions.domain.entity.QuestionTag;
import com.konfyrm.gigatester.questions.repository.QuestionTagRepository;
import com.konfyrm.gigatester.questions.service.QuestionTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service(QuestionTagServiceImpl.QUALIFIER)
public class QuestionTagServiceImpl implements QuestionTagService {

    public static final String QUALIFIER = "questionTagService";

    private final QuestionTagRepository repository;

    @Autowired
    public QuestionTagServiceImpl(QuestionTagRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<QuestionTag> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<QuestionTag> find(UUID tagId) {
        return repository.findById(tagId);
    }

    @Override
    public QuestionTag save(QuestionTag tag) {
        return repository.save(tag);
    }

    @Override
    public void delete(UUID tagId) {
        repository.deleteById(tagId);
    }

}
