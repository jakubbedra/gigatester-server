package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface CrosswordStateController {

    @GetMapping("api/v1/crossword-states/{id}")
    ResponseEntity<?> getCrosswordState(@PathVariable("id") UUID id);

    @PostMapping("api/v1/crossword-states")
    ResponseEntity<?> createCrosswordState(@RequestBody CrosswordStateRequest request);

    @PutMapping("api/v1/crossword-states/{id}")
    ResponseEntity<?> createCrosswordState(@PathVariable("id") UUID id, @RequestBody CrosswordStateUpdateRequest request);

}
