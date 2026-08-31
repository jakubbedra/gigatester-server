package com.konfyrm.gigatester.calendar.domain.entity;

import com.konfyrm.gigatester.calendar.domain.CalendarLinkType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.UUID;

/** One linked resource (a test or crossword) attached to a CalendarEvent. An event can have several. */
@Embeddable
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarEventLink {

    @Enumerated(EnumType.STRING)
    private CalendarLinkType linkType;

    private UUID linkId;

}
