package com.konfyrm.gigatester.crosswords.service;

/**
 * Case-normalizes crossword letters for grid storage/comparison. Latin
 * letters are uppercased as usual, but Greek letters (e.g. a term/clue
 * containing "β") are left as-authored — {@code Character.toUpperCase} would
 * otherwise turn them into their capital form (Β), which looks like a
 * different, unintended glyph rather than a "shouted" version of the same one.
 */
public final class CrosswordTextUtils {

    private CrosswordTextUtils() {}

    public static String toGridCase(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append(toGridCase(s.charAt(i)));
        }
        return sb.toString();
    }

    public static char toGridCase(char c) {
        return isGreek(c) ? c : Character.toUpperCase(c);
    }

    private static boolean isGreek(char c) {
        return (c >= 0x0370 && c <= 0x03FF) || (c >= 0x1F00 && c <= 0x1FFF);
    }

}
