package com.konfyrm.gigatester.calendar.repository;

import com.konfyrm.gigatester.calendar.domain.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    List<CalendarEvent> findByCalendarGroup_IdInAndStartTimeBetween(Collection<UUID> groupIds, LocalDateTime from, LocalDateTime to);

    List<CalendarEvent> findByCalendarGroup_IdIn(Collection<UUID> groupIds);

    void deleteByCalendarGroup_Id(UUID groupId);

}
