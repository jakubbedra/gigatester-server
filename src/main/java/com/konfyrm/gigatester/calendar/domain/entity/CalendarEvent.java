package com.konfyrm.gigatester.calendar.domain.entity;

import com.konfyrm.gigatester.calendar.domain.CalendarLinkType;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A single event (e.g. an exam date) belonging to a CalendarGroup, optionally linking to a test or crossword. */
@Entity
@Table(name = "calendar_events")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_group_id", nullable = false)
    private CalendarGroup calendarGroup;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Nullable
    private LocalDateTime endTime;

    /**
     * Legacy single-link fields, kept only so events created before multi-link support
     * still resolve their one link (see CalendarEventService#toResponse). New/updated
     * events are written via {@link #links} instead — these are no longer set on save.
     */
    @Nullable
    @Enumerated(EnumType.STRING)
    private CalendarLinkType linkType;

    @Nullable
    private UUID linkId;

    /** Zero or more linked tests/crosswords for this event. */
    @ElementCollection
    @CollectionTable(name = "calendar_event_links", joinColumns = @JoinColumn(name = "calendar_event_id"))
    @Builder.Default
    private List<CalendarEventLink> links = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

}
