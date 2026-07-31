package com.konfyrm.gigatester.crosswords.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crosswords")
public class Crossword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrosswordTerm> terms;

    @ManyToMany
    @JoinTable(name = "crossword_authors",
        joinColumns = @JoinColumn(name = "crossword_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<User> authors = new ArrayList<>();

    /**
     * Set once at creation. Used to authorize writes on crosswords not yet
     * attached to any subject (and therefore not yet resolvable to a subject
     * group).
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

}