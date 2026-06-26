package com.konfyrm.gigatester.tags.repository;

import com.konfyrm.gigatester.tags.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByKey(String key);
}
