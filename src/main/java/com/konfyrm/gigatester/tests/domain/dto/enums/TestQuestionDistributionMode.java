package com.konfyrm.gigatester.tests.domain.dto.enums;

public enum TestQuestionDistributionMode {
    RANDOM,
    MAX_PER_TAG,
    /** Picks the questions this user has answered wrong most often, per type/tag filter. */
    WORST
}
