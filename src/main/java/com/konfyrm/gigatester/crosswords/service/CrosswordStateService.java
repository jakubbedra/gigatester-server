package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.service.CrosswordTurnService.TurnOutcome;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateRepository;
import com.konfyrm.gigatester.metrics.service.DailyStreakService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class CrosswordStateService {

    private final CrosswordStateRepository crosswordStateRepository;
    private final CrosswordRepository crosswordRepository;
    private final CrosswordGeneratorService crosswordGeneratorService;
    private final CrosswordTurnService crosswordTurnService;
    private final SubjectGroupAccessService accessService;
    private final DailyStreakService streakService;

    @Autowired
    public CrosswordStateService(
            CrosswordStateRepository crosswordStateRepository,
            CrosswordRepository crosswordRepository,
            CrosswordGeneratorService crosswordGeneratorService,
            CrosswordTurnService crosswordTurnService,
            SubjectGroupAccessService accessService,
            DailyStreakService streakService
    ) {
        this.crosswordStateRepository = crosswordStateRepository;
        this.crosswordRepository = crosswordRepository;
        this.crosswordGeneratorService = crosswordGeneratorService;
        this.crosswordTurnService = crosswordTurnService;
        this.accessService = accessService;
        this.streakService = streakService;
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
        CrosswordState state = findCrosswordState(id, userId);
        TurnOutcome outcome = crosswordTurnService.processTurn(state, request.getLetters());
        if (state.getUser() != null && !state.getCurrentGrid().contains(String.valueOf(CrosswordState.UNCOVERED_FIELD))) {
            streakService.recordActivity(state.getUser());
        }
        return outcome;
    }

    public Optional<CrosswordState> findUserCrosswordState(UUID crosswordId, UUID userId) {
        return crosswordStateRepository.findFirstByCrossword_IdAndUser_Id(crosswordId, userId);
    }

    public CrosswordState createCrosswordState(CrosswordStateRequest request, User user) {
        if (!accessService.hasAccessToCrossword(request.getCrosswordId(), user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        Crossword crossword = crosswordRepository.findById(request.getCrosswordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + request.getCrosswordId() + " not found."));
        crosswordStateRepository.findFirstByCrossword_IdAndUser_Id(request.getCrosswordId(), user.getId())
                .ifPresent(crosswordStateRepository::delete);
        CrosswordState state = crosswordGeneratorService.generate(crossword, request.getNumberOfWords());
        state.setUser(user);
        return crosswordStateRepository.save(state);
    }

}
