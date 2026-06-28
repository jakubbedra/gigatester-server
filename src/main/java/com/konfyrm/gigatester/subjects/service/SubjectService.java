package com.konfyrm.gigatester.subjects.service;

import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Subject addSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> findSubjects() {
        return subjectRepository.findAll();
    }

    public Subject findSubject(UUID subjectId) {
        return subjectRepository.findById(subjectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject with id: " + subjectId + " not found."));
    }

    @Transactional
    public void updateSubject(UUID subjectId, Subject subject) {
        Subject existing = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject with id: " + subjectId + " not found."));
        existing.setName(subject.getName());
        existing.setDescription(subject.getDescription());
        existing.setDifficulty(subject.getDifficulty());
        existing.setTests(subject.getTests());
        existing.setCrosswords(subject.getCrosswords());
    }

    public void deleteSubject(UUID subjectId) {
        if (subjectRepository.findById(subjectId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject with id: " + subjectId + " not found.");
        }
        subjectRepository.deleteById(subjectId);
    }

}