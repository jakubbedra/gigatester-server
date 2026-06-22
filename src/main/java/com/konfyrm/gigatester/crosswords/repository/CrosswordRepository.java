package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CrosswordRepository extends JpaRepository<Crossword, UUID> { }
