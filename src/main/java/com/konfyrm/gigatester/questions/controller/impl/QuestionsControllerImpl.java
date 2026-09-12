package com.konfyrm.gigatester.questions.controller.impl;

import com.konfyrm.gigatester.questions.controller.QuestionsController;
import com.konfyrm.gigatester.questions.domain.dto.QuestionDto;
import com.konfyrm.gigatester.questions.domain.dto.QuestionsListDto;
import com.konfyrm.gigatester.questions.domain.dto.responses.BulkAddQuestionsResponse;
import com.konfyrm.gigatester.questions.domain.dto.responses.QuestionsResponse;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.service.QuestionMappingService;
import com.konfyrm.gigatester.questions.service.QuestionService;
import com.konfyrm.gigatester.questions.service.impl.QuestionConversionServiceImpl;
import com.konfyrm.gigatester.questions.service.impl.QuestionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class QuestionsControllerImpl implements QuestionsController {

    private final QuestionService questionService;
    private final QuestionMappingService questionMappingService;

    @Autowired
    public QuestionsControllerImpl(
            @Qualifier(QuestionServiceImpl.QUALIFIER) QuestionService questionService,
            @Qualifier(QuestionConversionServiceImpl.QUALIFIER) QuestionMappingService questionMappingService
    ) {
        this.questionService = questionService;
        this.questionMappingService = questionMappingService;
    }

    @Override
    public ResponseEntity<?> addQuestion(QuestionDto request) {
        Question entity = questionMappingService.toEntity(request);
        Question savedEntity = questionService.saveQuestion(entity);
        return ResponseEntity.accepted().body(savedEntity.getId());
    }

    @Override
    public ResponseEntity<?> addQuestions(QuestionsListDto request) {
        List<QuestionDto> questions = request.getQuestions() == null ? List.of() : request.getQuestions();
        List<UUID> createdIds = new ArrayList<>();
        List<BulkAddQuestionsResponse.Failure> failures = new ArrayList<>();

        // Every question is attempted independently — one bad entry doesn't abort the rest,
        // and the caller gets back exactly which indices (if any) failed and why.
        for (int i = 0; i < questions.size(); i++) {
            try {
                Question entity = questionMappingService.toEntity(questions.get(i));
                Question saved = questionService.saveQuestion(entity);
                createdIds.add(saved.getId());
            } catch (Exception e) {
                failures.add(BulkAddQuestionsResponse.Failure.builder()
                        .index(i)
                        .message(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                        .build());
            }
        }

        return ResponseEntity.ok(BulkAddQuestionsResponse.builder()
                .createdIds(createdIds)
                .failures(failures)
                .build());
    }

    @Override
    public ResponseEntity<?> getQuestions() {
        return ResponseEntity.ok().body(QuestionsResponse.builder()
                .questions(questionService.findQuestions().stream().map(questionMappingService::toSummaryResponse).toList())
                .build());
    }

    @Override
    public ResponseEntity<?> getQuestion(UUID id) {
        return questionService.findQuestion(id)
                .map(question -> ResponseEntity.ok().body(questionMappingService.toDto(question)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> updateQuestion(UUID id, QuestionDto request) {
        Optional<Question> questionOptional = questionService.findQuestion(id);
        if (questionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Question existing = questionOptional.get();
        request.setId(existing.getId());
        // toEntity now resolves request.getTags() itself, so this no longer needs to carry
        // the existing tags over by hand (doing so as well would just duplicate them).
        Question updated = questionMappingService.toEntity(request);
        questionService.saveQuestion(updated);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> deleteQuestion(UUID id) {
        Optional<Question> questionOptional = questionService.findQuestion(id);
        if (questionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

}