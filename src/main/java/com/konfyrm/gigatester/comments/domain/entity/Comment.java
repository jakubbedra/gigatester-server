package com.konfyrm.gigatester.comments.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User user;

    private String content;

    @Column(columnDefinition = "timestamp default now()")
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_id")
    @Builder.Default
    private Set<UUID> likedBy = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "comment_dislikes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_id")
    @Builder.Default
    private Set<UUID> dislikedBy = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> responses;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
