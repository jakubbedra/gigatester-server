package com.konfyrm.gigatester.calendar.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

/** Public preview of what a /calendar/join/{token} link leads to, shown before the user logs in. */
@Getter
@Builder
public class CalendarJoinPreviewResponse {
    private String groupName;
    private String ownerUsername;
    private boolean alreadyMember;
}
