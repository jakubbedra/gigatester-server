package com.konfyrm.gigatester.subjects.service;

import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubjectGroupService {

    private final SubjectGroupRepository subjectGroupRepository;

    public SubjectGroupService(SubjectGroupRepository subjectGroupRepository) {
        this.subjectGroupRepository = subjectGroupRepository;
    }

    public SubjectGroup addSubjectGroup(SubjectGroup subjectGroup) {
        return subjectGroupRepository.save(subjectGroup);
    }

    public List<SubjectGroup> findSubjectGroups() {
        return subjectGroupRepository.findAll();
    }

    public SubjectGroup findSubjectGroup(UUID id) {
        return subjectGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubjectGroup with id: " + id + " not found."));
    }

    public void updateSubjectGroup(UUID id, SubjectGroup subjectGroup) {
        Optional<SubjectGroup> existing = subjectGroupRepository.findById(id);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubjectGroup with id: " + id + " not found.");
        }
        subjectGroupRepository.save(existing.get().toBuilder()
                .name(subjectGroup.getName())
                .tests(subjectGroup.getTests())
                .build());
    }

    public void deleteSubjectGroup(UUID id) {
        if (subjectGroupRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SubjectGroup with id: " + id + " not found.");
        }
        subjectGroupRepository.deleteById(id);
    }

}
