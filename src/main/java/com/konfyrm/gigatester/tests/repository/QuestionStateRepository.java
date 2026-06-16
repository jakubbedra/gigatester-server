package com.konfyrm.gigatester.tests.repository;

import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuestionStateRepository extends JpaRepository<QuestionState, UUID> { }