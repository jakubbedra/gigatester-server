package com.konfyrm.gigatester.graphs.repository;

import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GraphRepository extends JpaRepository<Graph, UUID> { }
