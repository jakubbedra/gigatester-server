package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class CrosswordStateService {

    private final CrosswordStateRepository crosswordStateRepository;

    public CrosswordStateService(CrosswordStateRepository crosswordStateRepository) {
        this.crosswordStateRepository = crosswordStateRepository;
    }

    public CrosswordState findCrosswordState(UUID id) {
        return crosswordStateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CrosswordState with id: " + id + " not found."));
    }

}
