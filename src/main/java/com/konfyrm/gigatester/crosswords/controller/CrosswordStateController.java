package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface CrosswordStateController {

    @GetMapping("api/v1/crossword-states/{id}")
    ResponseEntity<?> getCrosswordState(@PathVariable("id") UUID id);

    @PostMapping("api/v1/crossword-states")
    ResponseEntity<?> createCrosswordState(@RequestBody CrosswordStateRequest crosswordStateRequest);

}