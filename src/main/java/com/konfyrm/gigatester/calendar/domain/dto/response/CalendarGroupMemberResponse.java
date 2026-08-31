package com.konfyrm.gigatester.calendar.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CalendarGroupMemberResponse {
    private UUID userId;
    private String username;
}
