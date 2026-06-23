package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.converter.CrosswordStateConverter;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import com.konfyrm.gigatester.crosswords.service.CrosswordStateService;
import com.konfyrm.gigatester.crosswords.service.CrosswordTurnService.TurnOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CrosswordStateControllerImpl implements CrosswordStateController {

    private final CrosswordStateService crosswordStateService;
    private final CrosswordStateConverter crosswordStateConverter;

    @Autowired
    public CrosswordStateControllerImpl(
            CrosswordStateService crosswordStateService,
            CrosswordStateConverter crosswordStateConverter
    ) {
        this.crosswordStateService = crosswordStateService;
        this.crosswordStateConverter = crosswordStateConverter;
    }

    @Override
    public ResponseEntity<?> getCrosswordState(UUID id) {
        return ResponseEntity.ok(crosswordStateConverter.toResponse(crosswordStateService.findCrosswordState(id)));
    }

    @Override
    public ResponseEntity<?> createCrosswordState(CrosswordStateRequest crosswordStateRequest) {
        return ResponseEntity.ok(crosswordStateConverter.toResponse(crosswordStateService.createCrosswordState(crosswordStateRequest)));
    }

    @Override
    public ResponseEntity<?> createCrosswordState(UUID id, CrosswordStateUpdateRequest request) {
        TurnOutcome outcome = crosswordStateService.updateCrosswordState(id, request);
        return ResponseEntity.ok(crosswordStateConverter.toResponse(outcome.state(), outcome.result()));
    }

}
