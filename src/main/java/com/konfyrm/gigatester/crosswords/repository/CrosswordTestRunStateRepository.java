package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTestRunState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CrosswordTestRunStateRepository extends JpaRepository<CrosswordTestRunState, UUID> {

    Optional<CrosswordTestRunState> findFirstByCrossword_IdAndUser_Id(UUID crosswordId, UUID userId);

    void deleteByCrossword_IdAndUser_Id(UUID crosswordId, UUID userId);

}
