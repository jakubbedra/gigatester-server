package com.konfyrm.gigatester.calendar.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A named collection of calendar events, owned by the staff member who created
 * it. Admins can manage every group; a moderator can only manage groups they
 * own. Regular users need to be invited (added as a {@link CalendarGroupMember})
 * before they can see a group's events.
 */
@Entity
@Table(name = "calendar_groups")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Random token that resolves to this group at /calendar/join/{token} (shown as a
     * QR code). Anyone holding the link/code can join after logging in — no per-user
     * invite needed. Null until a manager generates one for the first time.
     */
    @Column(unique = true)
    private String inviteToken;

}
