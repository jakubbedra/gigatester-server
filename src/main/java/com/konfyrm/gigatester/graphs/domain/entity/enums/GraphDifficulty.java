package com.konfyrm.gigatester.graphs.domain.entity.enums;

/**
 * Fraction of guessable (non-blank-label) vertices revealed when a session
 * starts. IMPOSSIBLE always reveals exactly one vertex, as an anchor —
 * without it the player would have no label on the board to orient from.
 */
public enum GraphDifficulty {
    EASY(0.5),
    NORMAL(0.3),
    HARD(0.15),
    IMPOSSIBLE(0.0);

    private final double revealFraction;

    GraphDifficulty(double revealFraction) {
        this.revealFraction = revealFraction;
    }

    public double getRevealFraction() {
        return revealFraction;
    }
}
