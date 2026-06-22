package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.converter.CrosswordStateConverter;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.service.CrosswordStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CrosswordStateControllerImpl implements CrosswordStateController {

    private final CrosswordStateService crosswordStateService;
    private final CrosswordStateConverter crosswordStateConverter;

    public CrosswordStateControllerImpl(CrosswordStateService crosswordStateService,
                                        CrosswordStateConverter crosswordStateConverter) {
        this.crosswordStateService = crosswordStateService;
        this.crosswordStateConverter = crosswordStateConverter;
    }

    @Override
    public ResponseEntity<?> getCrosswordState(UUID id) {
        return ResponseEntity.ok(crosswordStateConverter.toResponse(crosswordStateService.findCrosswordState(id)));
    }

    @Override
    public ResponseEntity<?> createCrosswordState(CrosswordStateRequest crosswordStateRequest) {
        return null;
    }

}
