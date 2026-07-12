package com.konfyrm.gigatester.tests.controller;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
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
import com.konfyrm.gigatester.users.domain.entity.UserRole;
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
    private final SubjectGroupAccessService accessService;
    private final SubjectGroupRepository subjectGroupRepository;
    private final SubjectGroupAccessRepository subjectGroupAccessRepository;
    private final UserRepository userRepository;

    public TestControllerImpl(TestService testService, TestConverter testConverter,
                              QuestionRepository questionRepository, SubjectGroupAccessService accessService,
                              SubjectGroupRepository subjectGroupRepository,
                              SubjectGroupAccessRepository subjectGroupAccessRepository,
                              UserRepository userRepository) {
        this.testService = testService;
        this.testConverter = testConverter;
        this.questionRepository = questionRepository;
        this.accessService = accessService;
        this.subjectGroupRepository = subjectGroupRepository;
        this.subjectGroupAccessRepository = subjectGroupAccessRepository;
        this.userRepository = userRepository;
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
        requireModerator(user);
        Test test = testService.addAuthor(testId, userId);
        return ResponseEntity.ok(testConverter.toResponse(test));
    }

    @Override
    public ResponseEntity<?> removeAuthor(UUID testId, UUID userId, User user) {
        requireModerator(user);
        Test test = testService.removeAuthor(testId, userId);
        return ResponseEntity.ok(testConverter.toResponse(test));
    }

    @Override
    public ResponseEntity<?> getAuthorCandidates(UUID testId, User user) {
        requireModerator(user);
        Test test = testService.findTest(testId);
        Set<UUID> alreadyAuthors = test.getAuthors().stream().map(User::getId).collect(Collectors.toSet());

        Set<UUID> candidateIds = new LinkedHashSet<>();
        subjectGroupRepository.findByTests_Id(testId).forEach(group ->
            subjectGroupAccessRepository.findBySubjectGroup_IdAndStatus(group.getId(), SubjectGroupAccessStatus.APPROVED)
                .forEach(req -> candidateIds.add(req.getUser().getId()))
        );
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.MODERATOR || u.getRole() == UserRole.ADMIN)
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

    private void requireModerator(User user) {
        if (user == null || (user.getRole() != UserRole.MODERATOR && user.getRole() != UserRole.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
