package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CrosswordStateRepository extends JpaRepository<CrosswordState, UUID> {

    Optional<CrosswordState> findFirstByCrossword_IdAndUser_Id(UUID crosswordId, UUID userId);

    void deleteByUser_Id(UUID userId);

    /**
     * Row-locks the state for the duration of the enclosing transaction, so
     * two concurrent turn submissions (double-click, retry, multiple tabs)
     * can't both read the same pre-turn snapshot and race to persist their
     * own version of hand letters / the board — the second waits for the
     * first transaction to commit, then reads its result instead of stale data.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CrosswordState s where s.id = :id")
    Optional<CrosswordState> findByIdForUpdate(@Param("id") UUID id);

}
