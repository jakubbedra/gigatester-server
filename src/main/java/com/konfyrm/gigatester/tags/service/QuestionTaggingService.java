package com.konfyrm.gigatester.tags.service;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.service.QuestionService;
import com.konfyrm.gigatester.questions.service.impl.QuestionServiceImpl;
import com.konfyrm.gigatester.tags.entity.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional
public class QuestionTaggingService {

    private final QuestionService questionService;
    private final TagService tagService;

    public QuestionTaggingService(
            @Qualifier(QuestionServiceImpl.QUALIFIER) QuestionService questionService,
            TagService tagService
    ) {
        this.questionService = questionService;
        this.tagService = tagService;
    }

    public void addTag(UUID questionId, UUID tagId) {
        Question question = questionService.findQuestion(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found: " + questionId));
        Tag tag = tagService.findById(tagId);
        if (question.getTags().stream().noneMatch(t -> t.getId().equals(tagId))) {
            question.getTags().add(tag);
        }
    }

    public void removeTag(UUID questionId, UUID tagId) {
        Question question = questionService.findQuestion(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found: " + questionId));
        question.getTags().removeIf(t -> t.getId().equals(tagId));
    }

}
