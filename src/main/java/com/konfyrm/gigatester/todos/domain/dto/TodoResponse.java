package com.konfyrm.gigatester.todos.domain.dto;

import com.konfyrm.gigatester.todos.domain.TodoLinkType;
import com.konfyrm.gigatester.todos.domain.TodoStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class TodoResponse {
    private UUID id;
    private String text;
    private boolean done;
    private TodoStatus status;
    private LocalDate dueDate;
    private TodoLinkType linkType;
    private UUID linkId;
    /** Name of the linked entity; null when it no longer exists or the user has lost access. */
    private String linkName;
    private Instant createdAt;
}
