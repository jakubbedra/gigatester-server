package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CrosswordStateRepository extends JpaRepository<CrosswordState, UUID> {

    Optional<CrosswordState> findFirstByCrossword_IdAndUser_Id(UUID crosswordId, UUID userId);

    void deleteByUser_Id(UUID userId);

}
