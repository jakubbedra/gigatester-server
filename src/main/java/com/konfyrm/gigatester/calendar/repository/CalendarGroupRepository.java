package com.konfyrm.gigatester.calendar.repository;

import com.konfyrm.gigatester.calendar.domain.entity.CalendarGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarGroupRepository extends JpaRepository<CalendarGroup, UUID> {

    List<CalendarGroup> findByOwner_Id(UUID ownerId);

    Optional<CalendarGroup> findByInviteToken(String inviteToken);

}
