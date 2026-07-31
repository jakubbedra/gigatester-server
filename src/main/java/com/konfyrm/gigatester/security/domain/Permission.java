package com.konfyrm.gigatester.security.domain;

/**
 * Hardcoded permission catalog. New permissions require a code change; which
 * permissions a {@link Role} grants is configured in the database.
 */
public enum Permission {
    SUBJECTS_READ,
    SUBJECTS_WRITE,
    TESTS_READ,
    TESTS_WRITE,
    CROSSWORDS_READ,
    CROSSWORDS_WRITE;

    /**
     * WRITE implies READ for the same resource. SUBJECTS_WRITE additionally
     * implies every other permission: it grants write access to a subject and
     * "all their entities" (tests, crosswords, and by extension their read
     * permissions too).
     */
    public boolean impliedBy(Permission granted) {
        if (this == granted) return true;
        if (granted == SUBJECTS_WRITE) return true;
        return switch (this) {
            case SUBJECTS_READ -> granted == SUBJECTS_WRITE;
            case TESTS_READ -> granted == TESTS_WRITE;
            case CROSSWORDS_READ -> granted == CROSSWORDS_WRITE;
            default -> false;
        };
    }
}
