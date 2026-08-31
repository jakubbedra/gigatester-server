package com.konfyrm.gigatester.calendar.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CalendarInviteResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String ownerUsername;
}
