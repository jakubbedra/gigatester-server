package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectRequest;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface SubjectController {

    @PostMapping("api/v1/subjects")
    ResponseEntity<?> addSubject(@RequestBody SubjectRequest subjectRequest);

    @GetMapping("api/v1/subjects")
    ResponseEntity<?> getSubjects();

    @GetMapping("api/v1/subjects/{subjectId}")
    ResponseEntity<?> getSubject(@PathVariable("subjectId") UUID subjectId, @AuthenticationPrincipal User user);

    @PutMapping("api/v1/subjects/{subjectId}")
    ResponseEntity<?> updateSubject(@PathVariable("subjectId") UUID subjectId, @RequestBody SubjectRequest subjectRequest);

    @DeleteMapping("api/v1/subjects/{subjectId}")
    ResponseEntity<?> deleteSubject(@PathVariable("subjectId") UUID subjectId);

    @PostMapping("api/v1/subjects/{subjectId}/authors/{userId}")
    ResponseEntity<?> addAuthor(@PathVariable("subjectId") UUID subjectId,
                                @PathVariable("userId") UUID userId,
                                @AuthenticationPrincipal User user);

    @DeleteMapping("api/v1/subjects/{subjectId}/authors/{userId}")
    ResponseEntity<?> removeAuthor(@PathVariable("subjectId") UUID subjectId,
                                   @PathVariable("userId") UUID userId,
                                   @AuthenticationPrincipal User user);

    @GetMapping("api/v1/subjects/{subjectId}/author-candidates")
    ResponseEntity<?> getAuthorCandidates(@PathVariable("subjectId") UUID subjectId,
                                          @AuthenticationPrincipal User user);

}