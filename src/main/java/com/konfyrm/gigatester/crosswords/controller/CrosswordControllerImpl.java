package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.converter.CrosswordConverter;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordRequest;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.service.CrosswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CrosswordControllerImpl implements CrosswordController {

    private final CrosswordService crosswordService;
    private final CrosswordConverter crosswordConverter;

    public CrosswordControllerImpl(CrosswordService crosswordService, CrosswordConverter crosswordConverter) {
        this.crosswordService = crosswordService;
        this.crosswordConverter = crosswordConverter;
    }

    @Override
    public ResponseEntity<?> addCrossword(CrosswordRequest request) {
        Crossword entity = crosswordConverter.toEntity(request);
        Crossword saved = crosswordService.addCrossword(entity);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getCrosswords() {
        return ResponseEntity.ok(crosswordConverter.toResponse(crosswordService.findCrosswords()));
    }

    @Override
    public ResponseEntity<?> getCrossword(UUID id) {
        return ResponseEntity.ok(crosswordConverter.toResponse(crosswordService.findCrossword(id)));
    }

    @Override
    public ResponseEntity<?> updateCrossword(UUID id, CrosswordRequest request) {
        Crossword entity = crosswordConverter.toEntity(request);
        crosswordService.updateCrossword(id, entity);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteCrossword(UUID id) {
        crosswordService.deleteCrossword(id);
        return ResponseEntity.noContent().build();
    }

}
