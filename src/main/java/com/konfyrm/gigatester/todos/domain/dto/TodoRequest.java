package com.konfyrm.gigatester.todos.domain.dto;

import com.konfyrm.gigatester.todos.domain.TodoLinkType;
import com.konfyrm.gigatester.todos.domain.TodoStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TodoRequest {
    private String text;
    private Boolean done;
    private TodoStatus status;
    private LocalDate dueDate;
    private TodoLinkType linkType;
    private UUID linkId;
}
