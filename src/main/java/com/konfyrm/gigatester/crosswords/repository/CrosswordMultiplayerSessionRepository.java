package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordMultiplayerSession;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordMultiplayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CrosswordMultiplayerSessionRepository extends JpaRepository<CrosswordMultiplayerSession, UUID> {

    @Query("SELECT s FROM CrosswordMultiplayerSession s WHERE s.player1.id = :userId OR s.player2.id = :userId")
    List<CrosswordMultiplayerSession> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM CrosswordMultiplayerSession s WHERE s.crossword.id = :crosswordId AND s.player1.id = :userId AND s.status = :status")
    List<CrosswordMultiplayerSession> findByPlayer1AndCrosswordAndStatus(
            @Param("crosswordId") UUID crosswordId,
            @Param("userId") UUID userId,
            @Param("status") CrosswordMultiplayerStatus status
    );

}
