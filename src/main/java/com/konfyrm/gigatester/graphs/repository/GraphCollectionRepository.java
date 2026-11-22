package com.konfyrm.gigatester.graphs.repository;

import com.konfyrm.gigatester.graphs.domain.entity.GraphCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GraphCollectionRepository extends JpaRepository<GraphCollection, UUID> {
    List<GraphCollection> findByGraphs_Id(UUID graphId);
}
