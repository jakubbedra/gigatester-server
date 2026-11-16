package com.konfyrm.gigatester.questions.config;

import com.konfyrm.gigatester.questions.domain.entity.enums.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * question_contents.type is an ordinal enum, and Hibernate puts a CHECK constraint on it limiting the value to the
 * ordinals that existed when the table was created. ddl-auto=update never widens it, so after a new ContentType value
 * is added the constraint has to be rebuilt. This does it once, and does nothing when the constraint is already wide enough.
 */
@Component
public class ContentTypeConstraintMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentTypeConstraintMigration.class);
    private static final String CONSTRAINT = "question_contents_type_check";

    private final JdbcTemplate jdbc;

    public ContentTypeConstraintMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int maxOrdinal = ContentType.values().length - 1;
            List<String> definitions = jdbc.queryForList(
                    "SELECT pg_get_constraintdef(oid) FROM pg_constraint "
                            + "WHERE conrelid = 'question_contents'::regclass AND conname = ?",
                    String.class, CONSTRAINT);
            if (definitions.isEmpty() || definitions.get(0).contains("<= " + maxOrdinal)
                    || definitions.get(0).contains("AND " + maxOrdinal)) {
                return;
            }
            jdbc.execute("ALTER TABLE question_contents DROP CONSTRAINT " + CONSTRAINT);
            jdbc.execute("ALTER TABLE question_contents ADD CONSTRAINT " + CONSTRAINT
                    + " CHECK (type >= 0 AND type <= " + maxOrdinal + ")");
            log.info("Widened {} to allow ContentType ordinals 0..{}", CONSTRAINT, maxOrdinal);
        } catch (Exception e) {
            // Never block startup over this; the app works for the older content types either way.
            log.warn("Could not update {}: {}", CONSTRAINT, e.getMessage());
        }
    }
}
