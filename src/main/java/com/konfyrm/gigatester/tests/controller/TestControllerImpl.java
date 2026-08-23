package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.questions.service.QuestionMappingService;
import com.konfyrm.gigatester.questions.service.impl.QuestionConversionServiceImpl;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestQuestionDistributionMode;
import com.konfyrm.gigatester.tests.service.QuestionDistributionUtil;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupAccessRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.tests.domain.converter.TestConverter;
import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.service.TestService;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TestControllerImpl implements TestController {

    private final TestService testService;
    private final TestConverter testConverter;
    private final QuestionRepository questionRepository;
    private final QuestionMappingService questionMappingService;
    private final SubjectGroupAccessService accessService;
    private final SubjectGroupRepository subjectGroupRepository;
    private final SubjectGroupAccessRepository subjectGroupAccessRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public TestControllerImpl(TestService testService, TestConverter testConverter,
                              QuestionRepository questionRepository,
                              @org.springframework.beans.factory.annotation.Qualifier(QuestionConversionServiceImpl.QUALIFIER) QuestionMappingService questionMappingService,
                              SubjectGroupAccessService accessService,
                              SubjectGroupRepository subjectGroupRepository,
                              SubjectGroupAccessRepository subjectGroupAccessRepository,
                              UserRepository userRepository,
                              PermissionService permissionService) {
        this.testService = testService;
        this.testConverter = testConverter;
        this.questionRepository = questionRepository;
        this.questionMappingService = questionMappingService;
        this.accessService = accessService;
        this.subjectGroupRepository = subjectGroupRepository;
        this.subjectGroupAccessRepository = subjectGroupAccessRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    @Override
    public ResponseEntity<?> addTest(TestRequest testRequest, User user) {
        permissionService.require(permissionService.canCreate(user, Permission.TESTS_WRITE));
        Test entity = testConverter.toEntity(testRequest);
        entity.setCreatedBy(user);
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
    public ResponseEntity<?> updateTest(UUID testId, TestRequest testRequest, User user) {
        permissionService.require(permissionService.hasTestPermission(user, testId, Permission.TESTS_WRITE));
        Test test = testConverter.toEntity(testRequest);
        testService.updateTest(testId, test);
        return ResponseEntity.accepted().body(test.getId());
    }

    @Override
    public ResponseEntity<?> deleteTest(UUID testId, User user) {
        permissionService.require(permissionService.hasTestPermission(user, testId, Permission.TESTS_WRITE));
        testService.deleteTest(testId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> getQuestionCounts(UUID testId, List<UUID> tagIds, boolean excludeTags, boolean matchAllTags, int maxPerTag, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        boolean filtered = tagIds != null && !tagIds.isEmpty();
        if (filtered && !excludeTags && maxPerTag > 0) {
            long closed = countWithMaxPerTag(testId, TesterEntityType.CLOSED_QUESTION, tagIds, matchAllTags, maxPerTag);
            long open = countWithMaxPerTag(testId, TesterEntityType.OPEN_QUESTION, tagIds, matchAllTags, maxPerTag);
            long statement = countWithMaxPerTag(testId, TesterEntityType.STATEMENT_QUESTION, tagIds, matchAllTags, maxPerTag);
            return ResponseEntity.ok(Map.of(
                    "closedQuestionsCount", closed,
                    "openQuestionsCount", open,
                    "statementQuestionsCount", statement
            ));
        }
        int tagCount = filtered ? tagIds.size() : 0;
        long closed = !filtered
                ? questionRepository.countByTestIdAndType(testId, TesterEntityType.CLOSED_QUESTION.toString())
                : excludeTags && matchAllTags
                ? questionRepository.countByTestIdAndTypeExcludingAllTags(testId, TesterEntityType.CLOSED_QUESTION.toString(), tagIds, tagCount)
                : excludeTags
                ? questionRepository.countByTestIdAndTypeExcludingTags(testId, TesterEntityType.CLOSED_QUESTION.toString(), tagIds)
                : matchAllTags
                ? questionRepository.countByTestIdAndTypeAndAllTags(testId, TesterEntityType.CLOSED_QUESTION.toString(), tagIds, tagCount)
                : questionRepository.countByTestIdAndTypeAndTags(testId, TesterEntityType.CLOSED_QUESTION.toString(), tagIds);
        long open = !filtered
                ? questionRepository.countByTestIdAndType(testId, TesterEntityType.OPEN_QUESTION.toString())
                : excludeTags && matchAllTags
                ? questionRepository.countByTestIdAndTypeExcludingAllTags(testId, TesterEntityType.OPEN_QUESTION.toString(), tagIds, tagCount)
                : excludeTags
                ? questionRepository.countByTestIdAndTypeExcludingTags(testId, TesterEntityType.OPEN_QUESTION.toString(), tagIds)
                : matchAllTags
                ? questionRepository.countByTestIdAndTypeAndAllTags(testId, TesterEntityType.OPEN_QUESTION.toString(), tagIds, tagCount)
                : questionRepository.countByTestIdAndTypeAndTags(testId, TesterEntityType.OPEN_QUESTION.toString(), tagIds);
        long statement = !filtered
                ? questionRepository.countByTestIdAndType(testId, TesterEntityType.STATEMENT_QUESTION.toString())
                : excludeTags && matchAllTags
                ? questionRepository.countByTestIdAndTypeExcludingAllTags(testId, TesterEntityType.STATEMENT_QUESTION.toString(), tagIds, tagCount)
                : excludeTags
                ? questionRepository.countByTestIdAndTypeExcludingTags(testId, TesterEntityType.STATEMENT_QUESTION.toString(), tagIds)
                : matchAllTags
                ? questionRepository.countByTestIdAndTypeAndAllTags(testId, TesterEntityType.STATEMENT_QUESTION.toString(), tagIds, tagCount)
                : questionRepository.countByTestIdAndTypeAndTags(testId, TesterEntityType.STATEMENT_QUESTION.toString(), tagIds);
        return ResponseEntity.ok(Map.of(
                "closedQuestionsCount", closed,
                "openQuestionsCount", open,
                "statementQuestionsCount", statement
        ));
    }

    private long countWithMaxPerTag(UUID testId, TesterEntityType type, List<UUID> tagIds, boolean matchAllTags, int maxPerTag) {
        List<Question> pool = matchAllTags
                ? questionRepository.findRandomQuestionsByAllTags(testId, type.toString(), tagIds, tagIds.size())
                : questionRepository.findRandomQuestionsByTags(testId, type.toString(), tagIds);
        return QuestionDistributionUtil.apply(pool, tagIds, false, TestQuestionDistributionMode.MAX_PER_TAG, maxPerTag).size();
    }

    @Override
    public ResponseEntity<?> getTestQuestions(UUID testId, int page, int size, String q, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        int clampedSize = Math.min(Math.max(size, 1), 100);
        int offset = page * clampedSize;
        String search = q == null ? "" : q.trim();
        long total = questionRepository.countPagedByTestId(testId, search);
        var questions = questionRepository.findPagedByTestId(testId, search, clampedSize, offset)
                .stream().map(questionMappingService::toDto).toList();
        return ResponseEntity.ok(Map.of(
                "questions", questions,
                "total", total,
                "page", page,
                "size", clampedSize
        ));
    }

    @Override
    public ResponseEntity<?> getTestTags(UUID testId, User user) {
        if (!accessService.hasAccessToTest(testId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        List<Map<String, Object>> tags = questionRepository.findDistinctTagsByTestId(testId).stream()
                .map(row -> Map.<String, Object>of("id", row[0].toString(), "key", row[1].toString()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }

    @Override
    public ResponseEntity<?> addAuthor(UUID testId, UUID userId, User user) {
        permissionService.require(permissionService.hasTestPermission(user, testId, Permission.TESTS_WRITE));
        Test test = testService.addAuthor(testId, userId);
        return ResponseEntity.ok(testConverter.toResponse(test));
    }

    @Override
    public ResponseEntity<?> removeAuthor(UUID testId, UUID userId, User user) {
        permissionService.require(permissionService.hasTestPermission(user, testId, Permission.TESTS_WRITE));
        Test test = testService.removeAuthor(testId, userId);
        return ResponseEntity.ok(testConverter.toResponse(test));
    }

    @Override
    public ResponseEntity<?> getAuthorCandidates(UUID testId, User user) {
        permissionService.require(permissionService.hasTestPermission(user, testId, Permission.TESTS_WRITE));
        Test test = testService.findTest(testId);
        Set<UUID> alreadyAuthors = test.getAuthors().stream().map(User::getId).collect(Collectors.toSet());

        Set<UUID> candidateIds = new LinkedHashSet<>();
        subjectGroupRepository.findByTests_Id(testId).forEach(group ->
            subjectGroupAccessRepository.findBySubjectGroup_IdAndStatus(group.getId(), SubjectGroupAccessStatus.APPROVED)
                .forEach(req -> candidateIds.add(req.getUser().getId()))
        );
        userRepository.findAll().stream()
                .filter(permissionService::isStaff)
                .map(User::getId).forEach(candidateIds::add);

        List<UserResponse> candidates = candidateIds.stream()
                .filter(id -> !alreadyAuthors.contains(id))
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> UserResponse.builder().id(u.getId()).username(u.getUsername())
                        .role(u.getRole()).profilePictureUrl(u.getProfilePictureUrl()).build())
                .sorted(Comparator.comparing(UserResponse::getUsername))
                .collect(Collectors.toList());
        return ResponseEntity.ok(candidates);
    }

}
