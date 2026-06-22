package com.konfyrm.gigatester.questions.domain.dto.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum GradingRule {
    MANUAL('m'),
    IGNORE_CASE('c'),
    IGNORE_PUNCTUATION('p'),
    TRIM_WHITESPACE('t');

    private final char hash;

    private static final Map<Character, GradingRule> HASH_TO_GRADING_RULE = new HashMap<>();

    static {
        for (GradingRule value : GradingRule.values()) {
            HASH_TO_GRADING_RULE.put(value.hash, value);
        }
    }

    GradingRule(char hash) {
        this.hash = hash;
    }

    public static GradingRule fromHash(char hash) {
        GradingRule gradingRule = HASH_TO_GRADING_RULE.get(hash);
        if (gradingRule == null) {
            throw new IllegalArgumentException("Invalid grading rule hash: " + hash);
        }
        return gradingRule;
    }

    public static Set<GradingRule> fromHash(String hash) {
        return hash.chars()
                .mapToObj(c -> (char) c)
                .map(GradingRule::fromHash)
                .collect(Collectors.toSet());
    }

    public static String toHash(Set<GradingRule> gradingRules) {
        StringBuilder hash = new StringBuilder();
        for (GradingRule gradingRule : gradingRules) {
            hash.append(gradingRule.hash);
        }
        return hash.toString();
    }

    public char getHash() {
        return hash;
    }

}
