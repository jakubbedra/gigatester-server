package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.service.CrosswordTurnService.TurnOutcome;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class CrosswordStateService {

    private final CrosswordStateRepository crosswordStateRepository;
    private final CrosswordRepository crosswordRepository;
    private final CrosswordGeneratorService crosswordGeneratorService;
    private final CrosswordTurnService crosswordTurnService;

    @Autowired
    public CrosswordStateService(
            CrosswordStateRepository crosswordStateRepository,
            CrosswordRepository crosswordRepository,
            CrosswordGeneratorService crosswordGeneratorService,
            CrosswordTurnService crosswordTurnService
    ) {
        this.crosswordStateRepository = crosswordStateRepository;
        this.crosswordRepository = crosswordRepository;
        this.crosswordGeneratorService = crosswordGeneratorService;
        this.crosswordTurnService = crosswordTurnService;
    }

    public CrosswordState findCrosswordState(UUID id) {
        return crosswordStateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CrosswordState with id: " + id + " not found."));
    }

    public TurnOutcome updateCrosswordState(UUID id, CrosswordStateUpdateRequest request) {
        CrosswordState state = findCrosswordState(id);
        return crosswordTurnService.processTurn(state, request.getLetters());
    }

    public CrosswordState createCrosswordState(CrosswordStateRequest request) {
        Crossword crossword = crosswordRepository.findById(request.getCrosswordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + request.getCrosswordId() + " not found."));
        CrosswordState state = crosswordGeneratorService.generate(crossword, request.getNumberOfWords());
        return crosswordStateRepository.save(state);
    }

}
