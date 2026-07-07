package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTestRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CrosswordTestRunRepository extends JpaRepository<CrosswordTestRun, UUID> {
    Optional<CrosswordTestRun> findTopByCrossword_IdAndUser_IdAndCompletedTrueOrderByCreatedAtDesc(UUID crosswordId, UUID userId);
}
