package com.konfyrm.gigatester.graphs.repository;

import com.konfyrm.gigatester.graphs.domain.entity.GraphSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface GraphSessionRepository extends JpaRepository<GraphSession, UUID> {

    @Transactional
    void deleteByUser_Id(UUID userId);

    @Transactional
    void deleteByGraph_Id(UUID graphId);

}
