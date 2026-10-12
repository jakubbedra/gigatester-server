package com.konfyrm.gigatester.coding.repository;

import com.konfyrm.gigatester.coding.domain.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, UUID> {

    List<CodingSubmission> findByTask_IdAndUser_Id(UUID taskId, UUID userId);

    List<CodingSubmission> findByTask_Id(UUID taskId);

    List<CodingSubmission> findByTask_IdAndProblemIsNull(UUID taskId);

    @Transactional
    void deleteByProblem_Id(UUID problemId);

    @Transactional
    void deleteByUser_Id(UUID userId);

    @Transactional
    void deleteByTask_Id(UUID taskId);

}
