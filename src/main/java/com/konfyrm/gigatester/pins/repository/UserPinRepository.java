package com.konfyrm.gigatester.pins.repository;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.domain.entity.UserPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPinRepository extends JpaRepository<UserPin, UUID> {

    List<UserPin> findByUser_IdOrderByPinnedAtDesc(UUID userId);

    Optional<UserPin> findByUser_IdAndEntityTypeAndEntityId(UUID userId, PinnedEntityType entityType, UUID entityId);

    @Transactional
    void deleteByUser_IdAndEntityTypeAndEntityId(UUID userId, PinnedEntityType entityType, UUID entityId);

    @Transactional
    void deleteByEntityTypeAndEntityId(PinnedEntityType entityType, UUID entityId);
}
