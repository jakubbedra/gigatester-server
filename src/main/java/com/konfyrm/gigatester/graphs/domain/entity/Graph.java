package com.konfyrm.gigatester.graphs.domain.entity;

import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "graphs")
public class Graph {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    /**
     * The whole vertex/edge/hyperedge structure as JSON, verbatim from the
     * Graph Lab frontend's exportJson() shape. A hypergraph's variable-size
     * source/target sets don't map cleanly onto normalized join tables
     * without a join row per source and per target of every hyperedge; one
     * JSON blob per graph keeps storage to a single row regardless of graph
     * size, at the cost of only being queryable as a whole — acceptable
     * here since nothing ever needs to search "graphs containing vertex X".
     */
    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String graphJson;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

}
