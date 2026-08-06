package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CompleteTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.StartTestRunRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.UpdateTestRunProgressRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.StartTestRunResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TestRunProgressResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TestRunResultResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TestRunTermResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTestRun;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTestRunState;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordTestRunRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordTestRunStateRepository;
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
    private final CrosswordTestRunStateRepository testRunStateRepository;
    private final CrosswordRepository crosswordRepository;
    private final SubjectGroupAccessService accessService;

    @Autowired
    public CrosswordTestRunService(
            CrosswordTestRunRepository testRunRepository,
            CrosswordTestRunStateRepository testRunStateRepository,
            CrosswordRepository crosswordRepository,
            SubjectGroupAccessService accessService
    ) {
        this.testRunRepository = testRunRepository;
        this.testRunStateRepository = testRunStateRepository;
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
        String mode = request.getMode() == null ? "ALL" : request.getMode();

        Optional<CrosswordTestRunState> existing = testRunStateRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, user.getId());
        Map<UUID, CrosswordTerm> termsById = crossword.getTerms().stream()
                .collect(Collectors.toMap(CrosswordTerm::getId, t -> t));

        if (request.isResume() && existing.isPresent() && mode.equalsIgnoreCase(existing.get().getMode())) {
            CrosswordTestRunState state = existing.get();
            List<UUID> orderedIds = parseIds(state.getTermIds());
            List<CrosswordTerm> orderedTerms = orderedIds.stream()
                    .map(termsById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            // Resumable only if it's still in-progress and every term still exists on the crossword.
            if (!orderedTerms.isEmpty() && orderedTerms.size() == orderedIds.size() && state.getCurrentIndex() < orderedTerms.size()) {
                return StartTestRunResponse.builder()
                        .terms(toTermResponses(orderedTerms))
                        .resumedIndex(state.getCurrentIndex())
                        .resumedWrongTermIds(parseIds(state.getWrongTermIds()))
                        .build();
            }
        }
        // Not resuming (either not requested, no in-progress run, mode mismatch, or it turned out stale/unresumable) — start fresh.
        existing.ifPresent(testRunStateRepository::delete);

        List<CrosswordTerm> terms = selectTerms(crossword, crosswordId, mode, user);
        Collections.shuffle(terms);

        CrosswordTestRunState state = CrosswordTestRunState.builder()
                .crossword(crossword)
                .user(user)
                .termIds(terms.stream().map(t -> t.getId().toString()).collect(Collectors.joining(",")))
                .mode(mode)
                .currentIndex(0)
                .wrongTermIds("")
                .build();
        testRunStateRepository.save(state);

        return StartTestRunResponse.builder()
                .terms(toTermResponses(terms))
                .resumedIndex(0)
                .resumedWrongTermIds(List.of())
                .build();
    }

    private List<CrosswordTerm> selectTerms(Crossword crossword, UUID crosswordId, String mode, User user) {
        if ("WRONG_ONLY".equalsIgnoreCase(mode)) {
            Optional<CrosswordTestRun> latest = testRunRepository
                    .findTopByCrossword_IdAndUser_IdAndCompletedTrueOrderByCreatedAtDesc(crosswordId, user.getId());
            if (latest.isPresent() && latest.get().getWrongTermIds() != null && !latest.get().getWrongTermIds().isBlank()) {
                Set<UUID> wrongIds = new HashSet<>(parseIds(latest.get().getWrongTermIds()));
                return crossword.getTerms().stream()
                        .filter(t -> wrongIds.contains(t.getId()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>(crossword.getTerms());
        }
        return new ArrayList<>(crossword.getTerms());
    }

    private List<TestRunTermResponse> toTermResponses(List<CrosswordTerm> terms) {
        return terms.stream()
                .map(t -> TestRunTermResponse.builder()
                        .id(t.getId())
                        .clue(t.getClue())
                        .clueType(t.getClueType() != null ? t.getClueType().name() : null)
                        .term(t.getTerm())
                        .termLength(t.getTerm() != null ? t.getTerm().length() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateProgress(UUID crosswordId, UpdateTestRunProgressRequest request, User user) {
        CrosswordTestRunState state = testRunStateRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No in-progress test run to update"));
        state.setCurrentIndex(request.getCurrentIndex());
        List<UUID> wrongTermIds = request.getWrongTermIds() != null ? request.getWrongTermIds() : List.of();
        state.setWrongTermIds(wrongTermIds.stream().map(UUID::toString).collect(Collectors.joining(",")));
        testRunStateRepository.save(state);
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

        testRunStateRepository.deleteByCrossword_IdAndUser_Id(crosswordId, user.getId());

        return toResult(run, wrongTermIds);
    }

    public Optional<TestRunProgressResponse> getInProgressRun(UUID crosswordId, User user) {
        return testRunStateRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, user.getId())
                .map(state -> {
                    List<UUID> termIds = parseIds(state.getTermIds());
                    return TestRunProgressResponse.builder()
                            .mode(state.getMode())
                            .currentIndex(state.getCurrentIndex())
                            .totalTerms(termIds.size())
                            .wrongTermCount(parseIds(state.getWrongTermIds()).size())
                            .build();
                });
    }

    public Optional<TestRunResultResponse> getLatestRun(UUID crosswordId, User user) {
        return testRunRepository.findTopByCrossword_IdAndUser_IdAndCompletedTrueOrderByCreatedAtDesc(crosswordId, user.getId())
                .map(run -> {
                    List<UUID> wrongIds = parseIds(run.getWrongTermIds());
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

    private List<UUID> parseIds(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }
}
