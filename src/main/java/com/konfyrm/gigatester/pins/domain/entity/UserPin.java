package com.konfyrm.gigatester.pins.domain.entity;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "user_pins",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "entity_type", "entity_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private PinnedEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Builder.Default
    @Column(name = "pinned_at", nullable = false)
    private LocalDateTime pinnedAt = LocalDateTime.now();

}
