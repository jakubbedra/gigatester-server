package com.konfyrm.gigatester.metrics.repository;

import com.konfyrm.gigatester.metrics.domain.entity.UserTestStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTestStatRepository extends JpaRepository<UserTestStat, UUID> {
    List<UserTestStat> findByUser_Id(UUID userId);
}
