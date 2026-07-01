package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateTermRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrosswordService {

    private final CrosswordRepository crosswordRepository;
    private final CrosswordStateTermRepository crosswordStateTermRepository;

    public CrosswordService(CrosswordRepository crosswordRepository,
                            CrosswordStateTermRepository crosswordStateTermRepository) {
        this.crosswordRepository = crosswordRepository;
        this.crosswordStateTermRepository = crosswordStateTermRepository;
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

    @Transactional
    public void updateCrossword(UUID id, Crossword incoming) {
        Crossword existing = crosswordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + id + " not found."));

        existing.setName(incoming.getName());

        Set<String> incomingTerms = incoming.getTerms().stream()
                .map(t -> t.getTerm().toUpperCase())
                .collect(Collectors.toSet());

        // Remove terms no longer in the list, but only if no game state references them
        existing.getTerms().removeIf(t ->
                !incomingTerms.contains(t.getTerm().toUpperCase())
                && !crosswordStateTermRepository.existsByCrosswordTermId(t.getId())
        );

        Set<String> remainingTerms = existing.getTerms().stream()
                .map(t -> t.getTerm().toUpperCase())
                .collect(Collectors.toSet());

        // Update clue/clueType for terms that already exist, add genuinely new ones
        for (CrosswordTerm incomingTerm : incoming.getTerms()) {
            String upper = incomingTerm.getTerm().toUpperCase();
            if (remainingTerms.contains(upper)) {
                existing.getTerms().stream()
                        .filter(t -> t.getTerm().equalsIgnoreCase(incomingTerm.getTerm()))
                        .findFirst()
                        .ifPresent(t -> {
                            t.setClue(incomingTerm.getClue());
                            t.setClueType(incomingTerm.getClueType());
                            t.getTags().clear();
                            if (incomingTerm.getTags() != null) {
                                t.getTags().addAll(incomingTerm.getTags());
                            }
                        });
            } else {
                existing.getTerms().add(incomingTerm);
            }
        }

        crosswordRepository.save(existing);
    }

    public void deleteCrossword(UUID id) {
        if (!crosswordRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + id + " not found.");
        }
        crosswordRepository.deleteById(id);
    }

}
