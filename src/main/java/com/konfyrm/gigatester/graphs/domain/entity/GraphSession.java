package com.konfyrm.gigatester.graphs.domain.entity;

import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphDifficulty;
import com.konfyrm.gigatester.graphs.domain.entity.enums.GraphPlayMode;
import com.konfyrm.gigatester.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

/**
 * One player's "fill in the blanks" attempt at a {@link Graph}: which
 * vertices were revealed up front and which they've since solved. The
 * player's own correct/wrong guesses never touch the authored graph row —
 * each session is its own small, cheap-to-store record of progress.
 */
@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "graph_sessions")
public class GraphSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "graph_id")
    private Graph graph;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private GraphDifficulty difficulty;

    /** Null on sessions created before modes existed — treat as TYPING. */
    @Enumerated(EnumType.STRING)
    private GraphPlayMode mode;

    public GraphPlayMode effectiveMode() {
        return mode == null ? GraphPlayMode.TYPING : mode;
    }

    /** Vertex ids shown from the start — fixed at session creation, comma-joined. */
    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String revealedVertexIds;

    /** Vertex ids the player has since guessed correctly — comma-joined, grows as they play. */
    @Lob
    @JdbcTypeCode(org.hibernate.type.SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String solvedVertexIds;

    @Builder.Default
    private boolean completed = false;

}
