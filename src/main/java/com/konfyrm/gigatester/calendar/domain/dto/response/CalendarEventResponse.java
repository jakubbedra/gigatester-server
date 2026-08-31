package com.konfyrm.gigatester.calendar.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CalendarEventResponse {
    private UUID id;
    private UUID calendarGroupId;
    private String calendarGroupName;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<CalendarEventLinkResponse> links;
    private boolean canManage;
}
