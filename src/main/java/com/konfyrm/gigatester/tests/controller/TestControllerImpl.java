package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.tests.domain.converter.TestConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.service.TestService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class TestControllerImpl implements TestController {

    private final TestService testService;
    private final TestConverter testConverter;
    private final QuestionRepository questionRepository;
    private final SubjectGroupAccessService accessService;

    public TestControllerImpl(TestService testService, TestConverter testConverter,
                              QuestionRepository questionRepository, SubjectGroupAccessService accessService) {
        this.testService = testService;
        this.testConverter = testConverter;
        this.questionRepository = questionRepository;
        this.accessService = accessService;
    }

    @Override
    public ResponseEntity<?> addTest(TestRequest testRequest) {
        Test entity = testConverter.toEntity(testRequest);
        Test savedEntity = testService.addTest(entity);
        return ResponseEntity.accepted().body(savedEntity.getId());
    }

    @Override
    public ResponseEntity<?> getTests() {
        return ResponseEntity.ok(testConverter.toResponse(testService.findTests()));
    }

    @Override
    public ResponseEntity<?> getTest(UUID testId, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return ResponseEntity.ok(testConverter.toResponse(testService.findTest(testId)));
    }

    @Override
    public ResponseEntity<?> updateTest(UUID testId, TestRequest testRequest) {
        Test test = testConverter.toEntity(testRequest);
        testService.updateTest(testId, test);
        return ResponseEntity.accepted().body(test.getId());
    }

    @Override
    public ResponseEntity<?> deleteTest(UUID testId) {
        testService.deleteTest(testId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getQuestionCounts(UUID testId, List<UUID> tagIds, boolean excludeTags, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        boolean filtered = tagIds != null && !tagIds.isEmpty();
        long closed = !filtered
                ? questionRepository.countByTestIdAndType(testId, TesterEntityType.CLOSED_QUESTION.toString())
                : excludeTags
                ? questionRepository.countByTestIdAndTypeExcludingTags(testId, TesterEntityType.CLOSED_QUESTION.toString(), tagIds)
                : questionRepository.countByTestIdAndTypeAndTags(testId, TesterEntityType.CLOSED_QUESTION.toString(), tagIds);
        long open = !filtered
                ? questionRepository.countByTestIdAndType(testId, TesterEntityType.OPEN_QUESTION.toString())
                : excludeTags
                ? questionRepository.countByTestIdAndTypeExcludingTags(testId, TesterEntityType.OPEN_QUESTION.toString(), tagIds)
                : questionRepository.countByTestIdAndTypeAndTags(testId, TesterEntityType.OPEN_QUESTION.toString(), tagIds);
        long statement = !filtered
                ? questionRepository.countByTestIdAndType(testId, TesterEntityType.STATEMENT_QUESTION.toString())
                : excludeTags
                ? questionRepository.countByTestIdAndTypeExcludingTags(testId, TesterEntityType.STATEMENT_QUESTION.toString(), tagIds)
                : questionRepository.countByTestIdAndTypeAndTags(testId, TesterEntityType.STATEMENT_QUESTION.toString(), tagIds);
        return ResponseEntity.ok(Map.of(
                "closedQuestionsCount", closed,
                "openQuestionsCount", open,
                "statementQuestionsCount", statement
        ));
    }

}
