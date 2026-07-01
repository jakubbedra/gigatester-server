package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateUpdateRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface CrosswordStateController {

    @GetMapping("api/v1/crossword-states/{id}")
    ResponseEntity<?> getCrosswordState(@PathVariable("id") UUID id,
                                        @AuthenticationPrincipal User user);

    @GetMapping("api/v1/crossword-states/by-crossword/{crosswordId}")
    ResponseEntity<?> getUserCrosswordState(@PathVariable("crosswordId") UUID crosswordId,
                                            @AuthenticationPrincipal User user);

    @PostMapping("api/v1/crossword-states")
    ResponseEntity<?> createCrosswordState(@RequestBody CrosswordStateRequest request,
                                           @AuthenticationPrincipal User user);

    @PostMapping("api/v1/crossword-states/generate")
    ResponseEntity<?> startGeneration(@RequestBody CrosswordStateRequest request,
                                      @AuthenticationPrincipal User user);

    @GetMapping("api/v1/crossword-states/jobs/{jobId}")
    ResponseEntity<?> getJobStatus(@PathVariable("jobId") UUID jobId);

    @DeleteMapping("api/v1/crossword-states/jobs/{jobId}")
    ResponseEntity<?> cancelJob(@PathVariable("jobId") UUID jobId);

    @PutMapping("api/v1/crossword-states/{id}")
    ResponseEntity<?> updateCrosswordState(@PathVariable("id") UUID id,
                                           @RequestBody CrosswordStateUpdateRequest request,
                                           @AuthenticationPrincipal User user);

}
