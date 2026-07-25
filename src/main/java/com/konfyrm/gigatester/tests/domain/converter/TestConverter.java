package com.konfyrm.gigatester.tests.domain.converter;


import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.tests.domain.dto.request.TestRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.TestResponse;
import com.konfyrm.gigatester.tests.domain.dto.response.TestsResponse;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
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
                .closedQuestionsCount(request.getClosedQuestionsCount())
                .openQuestionsCount(request.getOpenQuestionsCount())
                .passingPercentage(request.getPassingPercentage())
                .timeLimit(request.getTimeLimit())
                .hideTagsDuringTest(request.isHideTagsDuringTest())
                .build();
    }

    public TestResponse toResponse(Test test) {
        return TestResponse.builder()
                .id(test.getId())
                .name(test.getName())
                .questions(test.getQuestions().stream().map(Question::getId).toList())
                .closedQuestionsCount(test.getClosedQuestionsCount())
                .openQuestionsCount(test.getOpenQuestionsCount())
                .storedClosedQuestionsCount((int) test.getQuestions().stream().filter(q -> q.getType() == TesterEntityType.CLOSED_QUESTION).count())
                .storedOpenQuestionsCount((int) test.getQuestions().stream().filter(q -> q.getType() == TesterEntityType.OPEN_QUESTION).count())
                .passingPercentage(test.getPassingPercentage())
                .timeLimit(test.getTimeLimit())
                .hideTagsDuringTest(Boolean.TRUE.equals(test.getHideTagsDuringTest()))
                .authors(test.getAuthors() == null ? List.of() : test.getAuthors().stream()
                        .map(u -> UserResponse.builder().id(u.getId()).username(u.getUsername())
                                .role(u.getRole()).profilePictureUrl(u.getProfilePictureUrl()).build())
                        .toList())
                .build();
    }

    public TestsResponse toResponse(List<Test> tests) {
        return new TestsResponse(tests.stream()
                .map(t -> TestsResponse.TestSummaryResponse.builder().id(t.getId()).name(t.getName()).build())
                .toList());
    }

}
