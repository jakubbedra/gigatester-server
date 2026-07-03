package com.konfyrm.gigatester.crosswords.controller;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface CrosswordController {

    @PostMapping("api/v1/crosswords")
    ResponseEntity<?> addCrossword(@RequestBody CrosswordRequest request);

    @GetMapping("api/v1/crosswords")
    ResponseEntity<?> getCrosswords();

    @GetMapping("api/v1/crosswords/{id}")
    ResponseEntity<?> getCrossword(@PathVariable("id") UUID id);

    @PutMapping("api/v1/crosswords/{id}")
    ResponseEntity<?> updateCrossword(@PathVariable("id") UUID id, @RequestBody CrosswordRequest request);

    @DeleteMapping("api/v1/crosswords/{id}")
    ResponseEntity<?> deleteCrossword(@PathVariable("id") UUID id);

    @PutMapping("api/v1/crosswords/{id}/authors/{userId}")
    ResponseEntity<?> addAuthor(@PathVariable("id") UUID id, @PathVariable("userId") UUID userId, @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/crosswords/{id}/authors/{userId}")
    ResponseEntity<?> removeAuthor(@PathVariable("id") UUID id, @PathVariable("userId") UUID userId, @AuthenticationPrincipal User user);

    @GetMapping("api/v1/crosswords/{id}/author-candidates")
    ResponseEntity<?> getAuthorCandidates(@PathVariable("id") UUID id, @AuthenticationPrincipal User user);

}
