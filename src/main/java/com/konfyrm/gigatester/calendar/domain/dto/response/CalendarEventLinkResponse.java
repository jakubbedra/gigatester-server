package com.konfyrm.gigatester.calendar.domain.dto.response;

import com.konfyrm.gigatester.calendar.domain.CalendarLinkType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CalendarEventLinkResponse {
    private CalendarLinkType linkType;
    private UUID linkId;
    /** Resolved display name of the linked test/crossword, if still resolvable. */
    private String linkName;
}
