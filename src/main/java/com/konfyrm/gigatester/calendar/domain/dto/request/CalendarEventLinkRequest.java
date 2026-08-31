package com.konfyrm.gigatester.calendar.domain.dto.request;

import com.konfyrm.gigatester.calendar.domain.CalendarLinkType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CalendarEventLinkRequest {
    private CalendarLinkType linkType;
    private UUID linkId;
}
