package com.konfyrm.gigatester.subjects.service;

import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public Subject addAuthor(UUID subjectId, UUID userId) {
        Subject subject = findSubject(subjectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (subject.getAuthors().stream().noneMatch(a -> a.getId().equals(userId))) {
            subject.getAuthors().add(user);
            subjectRepository.save(subject);
        }
        return subject;
    }

    @Transactional
    public Subject removeAuthor(UUID subjectId, UUID userId) {
        Subject subject = findSubject(subjectId);
        subject.getAuthors().removeIf(a -> a.getId().equals(userId));
        subjectRepository.save(subject);
        return subject;
    }

    public void deleteSubject(UUID subjectId) {
        if (subjectRepository.findById(subjectId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject with id: " + subjectId + " not found.");
        }
        subjectRepository.deleteById(subjectId);
    }

}