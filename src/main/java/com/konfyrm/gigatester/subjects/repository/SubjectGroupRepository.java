package com.konfyrm.gigatester.subjects.repository;

import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectGroupRepository extends JpaRepository<SubjectGroup, UUID> { }
