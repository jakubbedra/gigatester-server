package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
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
                .termDefinitionQuestionsCount(test.getTermDefinitionQuestionsCount())
                .passingPercentage(test.getPassingPercentage())
                .build());
    }

    public void deleteTest(UUID testId) {
        if (testRepository.findById(testId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test with id: " + testId + " not found.");
        }
        testRepository.deleteById(testId);
    }

}
