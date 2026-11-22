package com.konfyrm.gigatester.graphs.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A named, ordered set of graphs that Graphle plays one after another. A collection has no access rules of
 * its own: a player gets the graphs of the collection they may already open.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "graph_collections")
public class GraphCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "graph_collection_graphs",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "graph_id")
    )
    @OrderColumn(name = "position")
    @Builder.Default
    private List<Graph> graphs = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
