package com.konfyrm.gigatester.coding.repository;

import com.konfyrm.gigatester.coding.domain.entity.CodingTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CodingTaskRepository extends JpaRepository<CodingTask, UUID> {

    List<CodingTask> findByStartAtIsNotNull();

}
