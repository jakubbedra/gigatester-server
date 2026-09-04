package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateTermRepository;
import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.repository.UserPinRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrosswordService {

    private final CrosswordRepository crosswordRepository;
    private final CrosswordStateTermRepository crosswordStateTermRepository;
    private final UserRepository userRepository;
    private final UserPinRepository userPinRepository;

    public CrosswordService(CrosswordRepository crosswordRepository,
                            CrosswordStateTermRepository crosswordStateTermRepository,
                            UserRepository userRepository,
                            UserPinRepository userPinRepository) {
        this.crosswordRepository = crosswordRepository;
        this.crosswordStateTermRepository = crosswordStateTermRepository;
        this.userRepository = userRepository;
        this.userPinRepository = userPinRepository;
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

        // Identity is the term's id, not its text — two terms can legitimately share the
        // same word (e.g. the same answer with two different clues), and matching by text
        // used to silently merge a "duplicate" addition into the existing term instead of
        // adding it as a second entry.
        Map<UUID, CrosswordTerm> existingById = existing.getTerms().stream()
                .collect(Collectors.toMap(CrosswordTerm::getId, t -> t));
        Set<UUID> incomingIds = incoming.getTerms().stream()
                .map(CrosswordTerm::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove terms no longer in the list, but only if no game state references them
        existing.getTerms().removeIf(t ->
                !incomingIds.contains(t.getId())
                && !crosswordStateTermRepository.existsByCrosswordTermId(t.getId())
        );

        // Update terms that already exist (matched by id), add genuinely new ones
        for (CrosswordTerm incomingTerm : incoming.getTerms()) {
            CrosswordTerm match = incomingTerm.getId() != null ? existingById.get(incomingTerm.getId()) : null;
            if (match != null && existing.getTerms().contains(match)) {
                match.setTerm(incomingTerm.getTerm());
                match.setClue(incomingTerm.getClue());
                match.setClueType(incomingTerm.getClueType());
                match.getTags().clear();
                if (incomingTerm.getTags() != null) {
                    match.getTags().addAll(incomingTerm.getTags());
                }
            } else {
                // Defensive: a stale/foreign id must not collide with — or overwrite — another row.
                incomingTerm.setId(null);
                existing.getTerms().add(incomingTerm);
            }
        }

        crosswordRepository.save(existing);
    }

    @Transactional
    public Crossword addAuthor(UUID crosswordId, UUID userId) {
        Crossword crossword = findCrossword(crosswordId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (crossword.getAuthors().stream().noneMatch(a -> a.getId().equals(userId))) {
            crossword.getAuthors().add(user);
            crosswordRepository.save(crossword);
        }
        return crossword;
    }

    @Transactional
    public Crossword removeAuthor(UUID crosswordId, UUID userId) {
        Crossword crossword = findCrossword(crosswordId);
        crossword.getAuthors().removeIf(a -> a.getId().equals(userId));
        crosswordRepository.save(crossword);
        return crossword;
    }

    @Transactional
    public void deleteCrossword(UUID id) {
        if (!crosswordRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword with id: " + id + " not found.");
        }
        userPinRepository.deleteByEntityTypeAndEntityId(PinnedEntityType.CROSSWORD, id);
        crosswordRepository.deleteById(id);
    }

}
