package com.konfyrm.gigatester.tests.domain.converter;


import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.TestResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.TestsResponse;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestConverter {

    private final QuestionRepository questionRepository;

    @Autowired
    public TestConverter(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Test toEntity(TestRequest request) {
        return Test.builder()
                .name(request.getName())
                .questions(request.getQuestions().stream()
                        .map(id -> questionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Question not found for id: " + id)))
                        .toList())
                .build();
    }

    public TestResponse toResponse(Test test) {
        return TestResponse.builder()
                .id(test.getId())
                .name(test.getName())
                .questions(test.getQuestions().stream().map(Question::getId).toList())
                .closedQuestionsCount(test.getClosedQuestionsCount())
                .openQuestionsCount(test.getOpenQuestionsCount())
                .termDefinitionQuestionsCount(test.getTermDefinitionQuestionsCount())
                .passingPercentage(test.getPassingPercentage())
                .build();
    }

    public TestsResponse toResponse(List<Test> tests) {
        return new TestsResponse(tests.stream()
                .map(t -> TestsResponse.TestSummaryResponse.builder().id(t.getId()).name(t.getName()).build())
                .toList());
    }

}