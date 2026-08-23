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
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordWrongPool;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordTestRunRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordTestRunStateRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordWrongPoolRepository;
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
    private final CrosswordWrongPoolRepository wrongPoolRepository;
    private final CrosswordRepository crosswordRepository;
    private final SubjectGroupAccessService accessService;

    @Autowired
    public CrosswordTestRunService(
            CrosswordTestRunRepository testRunRepository,
            CrosswordTestRunStateRepository testRunStateRepository,
            CrosswordWrongPoolRepository wrongPoolRepository,
            CrosswordRepository crosswordRepository,
            SubjectGroupAccessService accessService
    ) {
        this.testRunRepository = testRunRepository;
        this.testRunStateRepository = testRunStateRepository;
        this.wrongPoolRepository = wrongPoolRepository;
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
        // Starting fresh always discards any prior progress for this (user, crossword): a new run is a reset.
        existing.ifPresent(testRunStateRepository::delete);

        List<CrosswordTerm> terms = selectTerms(crossword, crosswordId, mode, user,
                request.getTagIds(), request.isExcludeTags(), request.isMatchAllTags());
        if (terms.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No terms match this filter");
        }

        // Starting a run (ALL or WRONG_ONLY) is a reset for whichever terms it covers
        // (the tag-filtered set, or everything if no filter) — clear just those from the
        // wrong pool, leaving pool entries for terms outside the filter untouched. Any
        // still answered wrong in this run will simply be re-added when it completes.
        clearFromWrongPool(crossword, user, terms.stream().map(CrosswordTerm::getId).collect(Collectors.toList()));

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

    /**
     * ALL mode: every crossword term, filtered by tag if requested — always the full
     * matching set, never reduced by past runs (starting a run is itself a reset).
     * WRONG_ONLY mode: the cumulative wrong-answer pool (terms answered wrong in any
     * run and not yet answered correctly since), further narrowed by tag if requested.
     */
    private List<CrosswordTerm> selectTerms(Crossword crossword, UUID crosswordId, String mode, User user,
                                             List<UUID> tagIds, boolean excludeTags, boolean matchAllTags) {
        List<CrosswordTerm> base;
        if ("WRONG_ONLY".equalsIgnoreCase(mode)) {
            Set<UUID> wrongIds = getWrongPoolIds(crosswordId, user.getId());
            base = crossword.getTerms().stream()
                    .filter(t -> wrongIds.contains(t.getId()))
                    .collect(Collectors.toList());
        } else {
            base = new ArrayList<>(crossword.getTerms());
        }

        String tagFilterMode = excludeTags ? "EXCLUDE" : null;
        return CrosswordTagFilter.filter(base, tagIds, tagFilterMode, matchAllTags);
    }

    private Set<UUID> getWrongPoolIds(UUID crosswordId, UUID userId) {
        return wrongPoolRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, userId)
                .map(p -> new HashSet<>(parseIds(p.getWrongTermIds())))
                .orElseGet(HashSet::new);
    }

    private void clearFromWrongPool(Crossword crossword, User user, List<UUID> termIds) {
        wrongPoolRepository.findFirstByCrossword_IdAndUser_Id(crossword.getId(), user.getId()).ifPresent(pool -> {
            Set<UUID> poolIds = new HashSet<>(parseIds(pool.getWrongTermIds()));
            if (poolIds.removeAll(termIds)) {
                pool.setWrongTermIds(poolIds.stream().map(UUID::toString).collect(Collectors.joining(",")));
                wrongPoolRepository.save(pool);
            }
        });
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

        Set<UUID> cumulativeWrongIds = testRunStateRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, user.getId())
                .map(state -> updateWrongPool(crossword, user, parseIds(state.getTermIds()), wrongTermIds))
                .orElseGet(() -> getWrongPoolIds(crosswordId, user.getId()));
        testRunStateRepository.deleteByCrossword_IdAndUser_Id(crosswordId, user.getId());

        return toResult(run, wrongTermIds, cumulativeWrongIds);
    }

    /**
     * Terms answered correctly this run are removed from the cumulative pool (they're
     * no longer "wrong"); terms answered wrong this run are added, even if they were
     * previously correct — getting it wrong now means it needs retesting again.
     */
    private Set<UUID> updateWrongPool(Crossword crossword, User user, List<UUID> attemptedTermIds, List<UUID> wrongTermIds) {
        Set<UUID> wrongIds = new HashSet<>(wrongTermIds);
        Set<UUID> correctIds = attemptedTermIds.stream()
                .filter(id -> !wrongIds.contains(id))
                .collect(Collectors.toSet());

        CrosswordWrongPool pool = wrongPoolRepository.findFirstByCrossword_IdAndUser_Id(crossword.getId(), user.getId())
                .orElseGet(() -> CrosswordWrongPool.builder()
                        .crossword(crossword)
                        .user(user)
                        .wrongTermIds("")
                        .build());

        Set<UUID> poolIds = new HashSet<>(parseIds(pool.getWrongTermIds()));
        poolIds.removeAll(correctIds);
        poolIds.addAll(wrongIds);
        pool.setWrongTermIds(poolIds.stream().map(UUID::toString).collect(Collectors.joining(",")));
        wrongPoolRepository.save(pool);
        return poolIds;
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
                    Set<UUID> cumulativeWrongIds = getWrongPoolIds(crosswordId, user.getId());
                    return toResult(run, wrongIds, cumulativeWrongIds);
                });
    }

    private TestRunResultResponse toResult(CrosswordTestRun run, List<UUID> wrongTermIds, Set<UUID> cumulativeWrongTermIds) {
        return TestRunResultResponse.builder()
                .id(run.getId())
                .wrongTermIds(wrongTermIds)
                .wrongTermCount(wrongTermIds.size())
                .totalTerms(run.getTotalTerms())
                .createdAt(run.getCreatedAt())
                .cumulativeWrongTermIds(new ArrayList<>(cumulativeWrongTermIds))
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
