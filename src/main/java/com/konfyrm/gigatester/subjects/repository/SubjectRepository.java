package com.konfyrm.gigatester.subjects.repository;

import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findByTests_Id(UUID testId);

    List<Subject> findByCrosswords_Id(UUID crosswordId);

}
