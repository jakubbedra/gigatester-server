package com.konfyrm.gigatester.subjects.repository;

import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findByTests_Id(UUID testId);

    List<Subject> findByCrosswords_Id(UUID crosswordId);

    @Modifying
    @Query(value = "DELETE FROM subject_authors WHERE user_id = :userId", nativeQuery = true)
    void removeUserFromAllAuthors(@Param("userId") UUID userId);

}
