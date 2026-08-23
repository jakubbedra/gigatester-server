package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.repository.UserPinRepository;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final UserPinRepository userPinRepository;

    public TestService(TestRepository testRepository, UserRepository userRepository, UserPinRepository userPinRepository) {
        this.testRepository = testRepository;
        this.userRepository = userRepository;
        this.userPinRepository = userPinRepository;
    }

    public Test addTest(Test test) {
        return testRepository.save(test);
    }

    public List<Test> findTests() {
        return testRepository.findAll();
    }

    public Test findTest(UUID testId) {
        return testRepository.findById(testId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test with id: " + testId + " not found."));
    }

    public void updateTest(UUID testId, Test test) {
        Optional<Test> testOptional = testRepository.findById(testId);
        if (testOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test with id: " + test.getId() + " not found.");
        }
        Test currentTest = testOptional.get();
        testRepository.save(currentTest.toBuilder()
                .name(test.getName())
                .questions(test.getQuestions())
                .openQuestionsCount(test.getOpenQuestionsCount())
                .closedQuestionsCount(test.getClosedQuestionsCount())
                .passingPercentage(test.getPassingPercentage())
                .timeLimit(test.getTimeLimit())
                .hideTagsDuringTest(test.getHideTagsDuringTest())
                .build());
    }

    public void addQuestionsToTest(UUID testId, List<Question> questions) {
        Test test = findTest(testId);
        List<Question> updated = new ArrayList<>(test.getQuestions());
        updated.addAll(questions);
        testRepository.save(test.toBuilder().questions(updated).build());
    }

    @Transactional
    public Test addAuthor(UUID testId, UUID userId) {
        Test test = findTest(testId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (test.getAuthors().stream().noneMatch(a -> a.getId().equals(userId))) {
            test.getAuthors().add(user);
            testRepository.save(test);
        }
        return test;
    }

    @Transactional
    public Test removeAuthor(UUID testId, UUID userId) {
        Test test = findTest(testId);
        test.getAuthors().removeIf(a -> a.getId().equals(userId));
        testRepository.save(test);
        return test;
    }

    @Transactional
    public void deleteTest(UUID testId) {
        if (testRepository.findById(testId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test with id: " + testId + " not found.");
        }
        userPinRepository.deleteByEntityTypeAndEntityId(PinnedEntityType.TEST, testId);
        testRepository.deleteById(testId);
    }

}
