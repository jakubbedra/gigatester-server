package com.konfyrm.gigatester.subjects.controller;

import com.konfyrm.gigatester.subjects.domain.converter.SubjectGroupConverter;
import com.konfyrm.gigatester.subjects.domain.dto.request.SubjectGroupRequest;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.service.SubjectGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SubjectGroupControllerImpl implements SubjectGroupController {

    private final SubjectGroupService subjectGroupService;
    private final SubjectGroupConverter subjectGroupConverter;

    public SubjectGroupControllerImpl(SubjectGroupService subjectGroupService, SubjectGroupConverter subjectGroupConverter) {
        this.subjectGroupService = subjectGroupService;
        this.subjectGroupConverter = subjectGroupConverter;
    }

    @Override
    public ResponseEntity<?> addSubjectGroup(SubjectGroupRequest request) {
        SubjectGroup entity = subjectGroupConverter.toEntity(request);
        SubjectGroup saved = subjectGroupService.addSubjectGroup(entity);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getSubjectGroups() {
        return ResponseEntity.ok(subjectGroupConverter.toResponse(subjectGroupService.findSubjectGroups()));
    }

    @Override
    public ResponseEntity<?> getSubjectGroup(UUID id) {
        return ResponseEntity.ok(subjectGroupConverter.toResponse(subjectGroupService.findSubjectGroup(id)));
    }

    @Override
    public ResponseEntity<?> updateSubjectGroup(UUID id, SubjectGroupRequest request) {
        SubjectGroup entity = subjectGroupConverter.toEntity(request);
        subjectGroupService.updateSubjectGroup(id, entity);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteSubjectGroup(UUID id) {
        subjectGroupService.deleteSubjectGroup(id);
        return ResponseEntity.noContent().build();
    }

}
