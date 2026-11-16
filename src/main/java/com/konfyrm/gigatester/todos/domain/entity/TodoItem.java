package com.konfyrm.gigatester.todos.domain.entity;

import com.konfyrm.gigatester.todos.domain.TodoLinkType;
import com.konfyrm.gigatester.todos.domain.TodoStatus;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "todo_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String text;

    @Builder.Default
    private boolean done = false;

    /** Kanban column. Null on rows created before the board existed; see {@link #effectiveStatus()}. */
    @Enumerated(EnumType.STRING)
    private TodoStatus status;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private TodoLinkType linkType;

    private UUID linkId;

    public TodoStatus effectiveStatus() {
        return status != null ? status : (done ? TodoStatus.DONE : TodoStatus.TODO);
    }

    /** Keeps the legacy done flag in step with the column. */
    public void applyStatus(TodoStatus newStatus) {
        this.status = newStatus;
        this.done = newStatus == TodoStatus.DONE;
    }

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
