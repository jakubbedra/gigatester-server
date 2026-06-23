package com.konfyrm.gigatester.crosswords.repository;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordStateTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CrosswordStateTermRepository extends JpaRepository<CrosswordStateTerm, UUID> {

    boolean existsByCrosswordTermId(UUID crosswordTermId);

}
