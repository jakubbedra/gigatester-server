package com.konfyrm.gigatester.calendar.repository;

import com.konfyrm.gigatester.calendar.domain.entity.CalendarGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarGroupMemberRepository extends JpaRepository<CalendarGroupMember, UUID> {

    List<CalendarGroupMember> findByCalendarGroup_Id(UUID groupId);

    List<CalendarGroupMember> findByUser_Id(UUID userId);

    Optional<CalendarGroupMember> findByCalendarGroup_IdAndUser_Id(UUID groupId, UUID userId);

    void deleteByCalendarGroup_IdAndUser_Id(UUID groupId, UUID userId);

    void deleteByCalendarGroup_Id(UUID groupId);

}
