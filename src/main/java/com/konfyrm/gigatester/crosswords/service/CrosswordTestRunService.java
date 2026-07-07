package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CompleteTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.StartTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.StartTestRunResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TestRunResultResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TestRunTermResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTestRun;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordTestRunRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrosswordTestRunService {

    private final CrosswordTestRunRepository testRunRepository;
    private final CrosswordRepository crosswordRepository;
    private final SubjectGroupAccessService accessService;

    @Autowired
    public CrosswordTestRunService(
            CrosswordTestRunRepository testRunRepository,
            CrosswordRepository crosswordRepository,
            SubjectGroupAccessService accessService
    ) {
        this.testRunRepository = testRunRepository;
        this.crosswordRepository = crosswordRepository;
        this.accessService = accessService;
    }

    @Transactional
    public StartTestRunResponse startTestRun(UUID crosswordId, StartTestRunRequest request, User user) {
        if (!accessService.hasAccessToCrossword(crosswordId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        Crossword crossword = crosswordRepository.findById(crosswordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword not found"));

        List<CrosswordTerm> terms;
        if ("WRONG_ONLY".equalsIgnoreCase(request.getMode())) {
            Optional<CrosswordTestRun> latest = testRunRepository
                    .findTopByCrossword_IdAndUser_IdAndCompletedTrueOrderByCreatedAtDesc(crosswordId, user.getId());
            if (latest.isPresent() && latest.get().getWrongTermIds() != null && !latest.get().getWrongTermIds().isBlank()) {
                Set<UUID> wrongIds = Arrays.stream(latest.get().getWrongTermIds().split(","))
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
                terms = crossword.getTerms().stream()
                        .filter(t -> wrongIds.contains(t.getId()))
                        .collect(Collectors.toList());
            } else {
                terms = new ArrayList<>(crossword.getTerms());
            }
        } else {
            terms = new ArrayList<>(crossword.getTerms());
        }

        Collections.shuffle(terms);

        List<TestRunTermResponse> termResponses = terms.stream()
                .map(t -> TestRunTermResponse.builder()
                        .id(t.getId())
                        .clue(t.getClue())
                        .clueType(t.getClueType() != null ? t.getClueType().name() : null)
                        .term(t.getTerm())
                        .termLength(t.getTerm() != null ? t.getTerm().length() : 0)
                        .build())
                .collect(Collectors.toList());

        return StartTestRunResponse.builder()
                .terms(termResponses)
                .build();
    }

    @Transactional
    public TestRunResultResponse completeTestRun(UUID crosswordId, CompleteTestRunRequest request, User user) {
        if (!accessService.hasAccessToCrossword(crosswordId, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        Crossword crossword = crosswordRepository.findById(crosswordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword not found"));

        List<UUID> wrongTermIds = request.getWrongTermIds() != null ? request.getWrongTermIds() : List.of();
        CrosswordTestRun run = CrosswordTestRun.builder()
                .crossword(crossword)
                .user(user)
                .wrongTermIds(wrongTermIds.stream().map(UUID::toString).collect(Collectors.joining(",")))
                .totalTerms(request.getTotalTerms())
                .completed(true)
                .createdAt(LocalDateTime.now())
                .build();
        testRunRepository.save(run);

        return toResult(run, wrongTermIds);
    }

    public Optional<TestRunResultResponse> getLatestRun(UUID crosswordId, User user) {
        return testRunRepository.findTopByCrossword_IdAndUser_IdAndCompletedTrueOrderByCreatedAtDesc(crosswordId, user.getId())
                .map(run -> {
                    List<UUID> wrongIds = parseWrongTermIds(run.getWrongTermIds());
                    return toResult(run, wrongIds);
                });
    }

    private TestRunResultResponse toResult(CrosswordTestRun run, List<UUID> wrongTermIds) {
        return TestRunResultResponse.builder()
                .id(run.getId())
                .wrongTermIds(wrongTermIds)
                .wrongTermCount(wrongTermIds.size())
                .totalTerms(run.getTotalTerms())
                .createdAt(run.getCreatedAt())
                .build();
    }

    private List<UUID> parseWrongTermIds(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }
}
