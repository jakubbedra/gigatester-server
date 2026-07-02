package com.konfyrm.gigatester.metrics.repository;

import com.konfyrm.gigatester.metrics.domain.entity.UserQuestionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserQuestionStatRepository extends JpaRepository<UserQuestionStat, UUID> {
    Optional<UserQuestionStat> findByUser_IdAndQuestion_Id(UUID userId, UUID questionId);
    List<UserQuestionStat> findByUser_IdAndQuestion_IdIn(UUID userId, List<UUID> questionIds);
    List<UserQuestionStat> findByUser_Id(UUID userId);

    void deleteByUser_Id(UUID userId);
}
