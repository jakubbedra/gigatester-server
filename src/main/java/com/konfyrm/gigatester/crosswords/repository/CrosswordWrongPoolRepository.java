package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordWrongPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CrosswordWrongPoolRepository extends JpaRepository<CrosswordWrongPool, UUID> {

    Optional<CrosswordWrongPool> findFirstByCrossword_IdAndUser_Id(UUID crosswordId, UUID userId);

}
