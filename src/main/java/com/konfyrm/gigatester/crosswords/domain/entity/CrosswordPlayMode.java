package com.konfyrm.gigatester.crosswords.domain.entity;

/**
 * How a crossword game (singleplayer-vs-bot, or multiplayer) is played. Shared by
 * {@link CrosswordState} (singleplayer) and {@link CrosswordMultiplayerSession}.
 *
 * <ul>
 *   <li>{@link #LETTERS} — the original mode: drag letters from a hand onto the grid,
 *       +1 per correct cell, -1 per wrong, plus a word-completion bonus.</li>
 *   <li>{@link #WORDS} — "Word Duel": no hand; on your turn you pick a clue and type the
 *       whole answer. A correct answer fills that word and scores exactly its length.
 *       A wrong answer scores nothing (never negative) and simply passes the turn.</li>
 * </ul>
 *
 * Null on rows created before this field existed — treat as {@link #LETTERS}
 * (see each entity's {@code effectiveMode()}).
 */
public enum CrosswordPlayMode {
    LETTERS,
    WORDS
}
