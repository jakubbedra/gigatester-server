package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CrosswordService {

    private final CrosswordRepository crosswordRepository;

    public CrosswordService(CrosswordRepository crosswordRepository) {
        this.crosswordRepository = crosswordRepository;
    }

    public Crossword addCrossword(Crossword crossword) {
        return crosswordRepository.save(crossword);
    }

    public List<Crossword> findCrosswords() {
        return crosswordRepository.findAll();
    }

    public Crossword findCrossword(UUID id) {
        return crosswordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + id + " not found."));
    }

    public void updateCrossword(UUID id, Crossword crossword) {
        Crossword existing = crosswordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + id + " not found."));
        crosswordRepository.save(existing.toBuilder()
                .name(crossword.getName())
                .terms(crossword.getTerms())
                .build());
    }

    public void deleteCrossword(UUID id) {
        if (!crosswordRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + id + " not found.");
        }
        crosswordRepository.deleteById(id);
    }

}
