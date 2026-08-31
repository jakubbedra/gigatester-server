package com.konfyrm.gigatester.calendar.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** Membership grant: a user invited into a CalendarGroup, letting them view its events. */
@Entity
@Table(
    name = "calendar_group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"calendar_group_id", "user_id"})
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_group_id", nullable = false)
    private CalendarGroup calendarGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Null/true = accepted (can view the group's events); false = a pending invite
     * awaiting the user's response, shown in their Inbox until accepted or declined.
     * Boxed and defaulted to null-means-accepted so rows created before this field
     * existed keep working without a migration.
     */
    @Column
    private Boolean accepted;

}
