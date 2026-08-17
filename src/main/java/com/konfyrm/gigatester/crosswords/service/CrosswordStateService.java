package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.BotDifficulty;
import com.konfyrm.gigatester.crosswords.service.CrosswordTurnService.TurnOutcome;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateRepository;
import com.konfyrm.gigatester.metrics.service.DailyStreakService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrosswordStateService {

    private final CrosswordStateRepository crosswordStateRepository;
    private final CrosswordRepository crosswordRepository;
    private final CrosswordGeneratorService crosswordGeneratorService;
    private final CrosswordTurnService crosswordTurnService;
    private final SubjectGroupAccessService accessService;
    private final DailyStreakService streakService;
    private final CrosswordGenerationJobStore jobStore;

    @Autowired
    public CrosswordStateService(
            CrosswordStateRepository crosswordStateRepository,
            CrosswordRepository crosswordRepository,
            CrosswordGeneratorService crosswordGeneratorService,
            CrosswordTurnService crosswordTurnService,
            SubjectGroupAccessService accessService,
            DailyStreakService streakService,
            CrosswordGenerationJobStore jobStore
    ) {
        this.crosswordStateRepository = crosswordStateRepository;
        this.crosswordRepository = crosswordRepository;
        this.crosswordGeneratorService = crosswordGeneratorService;
        this.crosswordTurnService = crosswordTurnService;
        this.accessService = accessService;
        this.streakService = streakService;
        this.jobStore = jobStore;
    }

    public CrosswordState findCrosswordState(UUID id) {
        return crosswordStateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CrosswordState with id: " + id + " not found."));
    }

    public CrosswordState findCrosswordState(UUID id, UUID userId) {
        CrosswordState state = findCrosswordState(id);
        if (state.getUser() == null || !state.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return state;
    }

    @Transactional
    public TurnOutcome updateCrosswordState(UUID id, CrosswordStateUpdateRequest request, UUID userId) {
        CrosswordState state = crosswordStateRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CrosswordState with id: " + id + " not found."));
        if (state.getUser() == null || !state.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        TurnOutcome outcome = crosswordTurnService.processTurn(state, request.getLetters());
        if (state.getUser() != null && !state.getCurrentGrid().contains(String.valueOf(CrosswordState.UNCOVERED_FIELD))) {
            streakService.recordActivity(state.getUser());
        }
        return outcome;
    }

    public Optional<CrosswordState> findUserCrosswordState(UUID crosswordId, UUID userId) {
        return crosswordStateRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, userId);
    }

    public UUID startAsyncGeneration(CrosswordStateRequest request, User user) {
        UUID jobId = jobStore.create();
        generateAsync(request, user, jobId);
        return jobId;
    }

    @Async
    @Transactional
    public void generateAsync(CrosswordStateRequest request, User user, UUID jobId) {
        try {
            if (jobStore.isCancelled(jobId)) return;
            CrosswordState state = createCrosswordState(request, user);
            if (jobStore.isCancelled(jobId)) return;
            jobStore.complete(jobId, state.getId());
        } catch (Exception e) {
            if (!jobStore.isCancelled(jobId)) {
                jobStore.fail(jobId, e.getMessage());
            }
        }
    }

    public void cancelJob(UUID jobId) {
        jobStore.cancel(jobId);
    }

    public CrosswordGenerationJobStore.JobResult getJobResult(UUID jobId) {
        return jobStore.get(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + jobId));
    }

    public CrosswordState createCrosswordState(CrosswordStateRequest request, User user) {
        if (!accessService.hasAccessToCrossword(request.getCrosswordId(), user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        Crossword crossword = crosswordRepository.findById(request.getCrosswordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + request.getCrosswordId() + " not found."));
        crosswordStateRepository.findFirstByCrossword_IdAndUser_Id(request.getCrosswordId(), user.getId())
                .ifPresent(crosswordStateRepository::delete);

        List<UUID> termIdFilter = request.getTermIdFilter();
        List<CrosswordTerm> filteredTerms = (termIdFilter != null && !termIdFilter.isEmpty())
                ? crossword.getTerms().stream()
                        .filter(t -> termIdFilter.contains(t.getId()))
                        .collect(Collectors.toList())
                : crossword.getTerms();
        filteredTerms = CrosswordTagFilter.filter(filteredTerms, request.getTagFilter(), request.getTagFilterMode(), request.isMatchAllTags());

        CrosswordState state = crosswordGeneratorService.generate(crossword, filteredTerms, request.getNumberOfWords());
        state.setUser(user);
        state.setBotDifficulty(request.getBotDifficulty() != null ? request.getBotDifficulty() : BotDifficulty.NORMAL);
        return crosswordStateRepository.save(state);
    }

}
