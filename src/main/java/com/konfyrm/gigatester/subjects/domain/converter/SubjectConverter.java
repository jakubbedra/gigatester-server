package com.konfyrm.gigatester.subjects.domain.converter;

import com.konfyrm.gigatester.comments.service.CommentService;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectRequest;
import com.konfyrm.gigatester.subjects.domain.dto.response.SubjectResponse;
import com.konfyrm.gigatester.subjects.domain.dto.response.SubjectsResponse;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SubjectConverter {

    private final TestRepository testRepository;
    private final CrosswordRepository crosswordRepository;
    private final CommentService commentService;

    @Autowired
    public SubjectConverter(TestRepository testRepository, CrosswordRepository crosswordRepository, CommentService commentService) {
        this.testRepository = testRepository;
        this.crosswordRepository = crosswordRepository;
        this.commentService = commentService;
    }

    public Subject toEntity(SubjectRequest request) {
        List<Crossword> crosswords = request.getCrosswords() == null ? List.of() :
                request.getCrosswords().stream()
                        .map(id -> crosswordRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Crossword not found for id: " + id)))
                        .toList();

        return Subject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .tests(request.getTests().stream()
                        .map(id -> testRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Test not found for id: " + id)))
                        .toList())
                .crosswords(crosswords)
                .build();
    }

    public SubjectResponse toResponse(Subject subject, UUID currentUserId) {
        List<UUID> crosswordIds = subject.getCrosswords() == null ? List.of() :
                subject.getCrosswords().stream().map(Crossword::getId).toList();

        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .description(subject.getDescription())
                .difficulty(subject.getDifficulty())
                .tests(subject.getTests().stream().map(Test::getId).toList())
                .crosswords(crosswordIds)
                .comments(subject.getComments() == null ? List.of() :
                        subject.getComments().stream().map(c -> commentService.toResponse(c, currentUserId)).toList())
                .authors(subject.getAuthors() == null ? List.of() :
                        subject.getAuthors().stream().map(this::toAuthorResponse).toList())
                .build();
    }

    private UserResponse toAuthorResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .build();
    }

    public SubjectsResponse toResponse(List<Subject> subjects) {
        return new SubjectsResponse(subjects.stream()
                .map(s -> SubjectsResponse.SubjectSummaryResponse.builder().id(s.getId()).name(s.getName()).build())
                .toList());
    }

}
