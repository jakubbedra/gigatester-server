package com.konfyrm.gigatester.questions.repository;

import com.konfyrm.gigatester.questions.domain.entity.QuestionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionReportRepository extends JpaRepository<QuestionReport, UUID> {

    List<QuestionReport> findByResolvedFalseOrderByCreatedAtDesc();

    List<QuestionReport> findAllByOrderByCreatedAtDesc();

    List<QuestionReport> findByTest_IdInAndResolvedFalseOrderByCreatedAtDesc(List<UUID> testIds);

    List<QuestionReport> findByTest_IdInOrderByCreatedAtDesc(List<UUID> testIds);

}
