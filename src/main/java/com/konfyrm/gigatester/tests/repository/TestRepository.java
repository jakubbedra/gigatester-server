package com.konfyrm.gigatester.tests.repository;

import com.konfyrm.gigatester.tests.domain.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TestRepository extends JpaRepository<Test, UUID> {

    List<Test> findByAuthors_Id(UUID userId);

}