package com.konfyrm.gigatester.calendar.domain.dto.request;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CalendarEventRequest {
    private UUID calendarGroupId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    @Nullable
    private LocalDateTime endTime;
    @Nullable
    private List<CalendarEventLinkRequest> links;
}
