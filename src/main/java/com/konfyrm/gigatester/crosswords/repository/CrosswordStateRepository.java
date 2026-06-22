package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CrosswordStateRepository extends JpaRepository<CrosswordState, UUID> { }
