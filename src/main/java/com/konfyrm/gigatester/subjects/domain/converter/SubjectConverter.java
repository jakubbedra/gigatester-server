package com.konfyrm.gigatester.subjects.domain.converter;

import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectRequest;
import com.konfyrm.gigatester.subjects.domain.dto.response.SubjectResponse;
import com.konfyrm.gigatester.subjects.domain.dto.response.SubjectsResponse;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubjectConverter {

    private final TestRepository testRepository;

    @Autowired
    public SubjectConverter(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public Subject toEntity(SubjectRequest request) {
        return Subject.builder()
                .name(request.getName())
                .tests(request.getTests().stream()
                        .map(id -> testRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Test not found for id: " + id)))
                        .toList())
                .build();
    }

    public SubjectResponse toResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .tests(subject.getTests().stream().map(Test::getId).toList())
                .build();
    }

    public SubjectsResponse toResponse(List<Subject> subjects) {
        return new SubjectsResponse(subjects.stream()
                .map(s -> SubjectsResponse.SubjectSummaryResponse.builder().id(s.getId()).name(s.getName()).build())
                .toList());
    }

}