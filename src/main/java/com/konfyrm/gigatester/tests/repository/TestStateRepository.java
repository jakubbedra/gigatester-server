package com.konfyrm.gigatester.tests.repository;

import com.konfyrm.gigatester.tests.domain.entity.TestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestStateRepository extends JpaRepository<TestState, UUID> {

    Optional<TestState> findFirstByTest_Id(UUID testId);

}
