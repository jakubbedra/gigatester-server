package com.konfyrm.gigatester.calendar.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CalendarGroupResponse {
    private UUID id;
    private String name;
    private UUID ownerId;
    private String ownerUsername;
    /**
     * Named "owner", not "isOwner" — Lombok would generate isOwner() for a boolean
     * field already prefixed "is", and Jackson serializes that by stripping "is",
     * producing JSON key "owner" regardless of the field name. Naming it "owner"
     * up front keeps the Java field, the getter, and the JSON key all in sync.
     */
    private boolean owner;
    private boolean canManage;
    private int memberCount;
    /** Only populated when canManage is true — the raw token behind /calendar/join/{token}. */
    private String inviteToken;
}
