package com.konfyrm.gigatester.subjects.repository;

import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> { }