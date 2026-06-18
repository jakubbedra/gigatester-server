package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface SubjectController {

    @PostMapping("api/v1/subjects")
    ResponseEntity<?> addSubject(@RequestBody SubjectRequest subjectRequest);

    @GetMapping("api/v1/subjects")
    ResponseEntity<?> getSubjects();

    @GetMapping("api/v1/subjects/{subjectId}")
    ResponseEntity<?> getSubject(@PathVariable("subjectId") UUID subjectId);

    @PutMapping("api/v1/subjects/{subjectId}")
    ResponseEntity<?> updateSubject(@PathVariable("subjectId") UUID subjectId, @RequestBody SubjectRequest subjectRequest);

    @DeleteMapping("api/v1/subjects/{subjectId}")
    ResponseEntity<?> deleteSubject(@PathVariable("subjectId") UUID subjectId);

}