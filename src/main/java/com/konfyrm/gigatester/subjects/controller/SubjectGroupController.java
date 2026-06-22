package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectGroupRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface SubjectGroupController {

    @PostMapping("api/v1/subject-groups")
    ResponseEntity<?> addSubjectGroup(@RequestBody SubjectGroupRequest request);

    @GetMapping("api/v1/subject-groups")
    ResponseEntity<?> getSubjectGroups();

    @GetMapping("api/v1/subject-groups/{id}")
    ResponseEntity<?> getSubjectGroup(@PathVariable("id") UUID id);

    @PutMapping("api/v1/subject-groups/{id}")
    ResponseEntity<?> updateSubjectGroup(@PathVariable("id") UUID id, @RequestBody SubjectGroupRequest request);

    @DeleteMapping("api/v1/subject-groups/{id}")
    ResponseEntity<?> deleteSubjectGroup(@PathVariable("id") UUID id);

}
