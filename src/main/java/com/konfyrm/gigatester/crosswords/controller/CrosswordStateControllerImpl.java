package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.converter.CrosswordStateConverter;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.service.CrosswordStateService;
import com.konfyrm.gigatester.crosswords.service.CrosswordTurnService.TurnOutcome;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
public class CrosswordStateControllerImpl implements CrosswordStateController {

    private final CrosswordStateService crosswordStateService;
    private final CrosswordStateConverter crosswordStateConverter;

    @Autowired
    public CrosswordStateControllerImpl(CrosswordStateService crosswordStateService,
                                        CrosswordStateConverter crosswordStateConverter) {
        this.crosswordStateService = crosswordStateService;
        this.crosswordStateConverter = crosswordStateConverter;
    }

    @Override
    public ResponseEntity<?> getCrosswordState(UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(crosswordStateConverter.toResponse(crosswordStateService.findCrosswordState(id, user.getId())));
    }

    @Override
    public ResponseEntity<?> getUserCrosswordState(UUID crosswordId, @AuthenticationPrincipal User user) {
        Optional<CrosswordState> stateOpt = crosswordStateService.findUserCrosswordState(crosswordId, user.getId());
        if (stateOpt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(crosswordStateConverter.toResponse(stateOpt.get()));
    }

    @Override
    public ResponseEntity<?> createCrosswordState(CrosswordStateRequest request, @AuthenticationPrincipal User user) {
        CrosswordState state = crosswordStateService.createCrosswordState(request, user);
        return ResponseEntity.ok(crosswordStateConverter.toResponse(state));
    }

    @Override
    public ResponseEntity<?> updateCrosswordState(UUID id, CrosswordStateUpdateRequest request,
                                                  @AuthenticationPrincipal User user) {
        TurnOutcome outcome = crosswordStateService.updateCrosswordState(id, request, user.getId());
        return ResponseEntity.ok(crosswordStateConverter.toResponse(outcome.state(), outcome.result()));
    }
}
