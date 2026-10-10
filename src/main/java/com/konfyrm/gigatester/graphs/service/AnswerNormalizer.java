package com.konfyrm.gigatester.graphs.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reduces a vertex label or a player's guess to a canonical form so that
 * "$NADH^{+} + H^{+} + CO_2$" and "nadh+ + h+ + co2" compare equal: LaTeX
 * markup is stripped, case, whitespace and diacritics are ignored, and Greek
 * letters compare by name ("\alpha", "α" and "alpha" are the same).
 */
final class AnswerNormalizer {

    private static final Pattern COMMAND = Pattern.compile("\\\\([a-zA-Z]+)");
    private static final Pattern ESCAPED_SYMBOL = Pattern.compile("\\\\[^a-zA-Z]");
    private static final Pattern MARKUP = Pattern.compile("[${}^_]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private static final Map<Character, String> GREEK = Map.ofEntries(
            Map.entry('α', "alpha"), Map.entry('β', "beta"), Map.entry('γ', "gamma"), Map.entry('δ', "delta"),
            Map.entry('ε', "epsilon"), Map.entry('ζ', "zeta"), Map.entry('η', "eta"), Map.entry('θ', "theta"),
            Map.entry('ι', "iota"), Map.entry('κ', "kappa"), Map.entry('λ', "lambda"), Map.entry('μ', "mu"),
            Map.entry('ν', "nu"), Map.entry('ξ', "xi"), Map.entry('π', "pi"), Map.entry('ρ', "rho"),
            Map.entry('σ', "sigma"), Map.entry('τ', "tau"), Map.entry('φ', "phi"), Map.entry('χ', "chi"),
            Map.entry('ψ', "psi"), Map.entry('ω', "omega"));

    private AnswerNormalizer() { }

    static String normalize(String input) {
        if (input == null) return "";
        // Keep command names that are Greek letters ("\alpha" -> "alpha"); drop all other commands (\text, \mathrm, \cdot...).
        String s = COMMAND.matcher(input).replaceAll(m -> {
            String name = m.group(1).toLowerCase(Locale.ROOT);
            boolean greek = GREEK.containsValue(name);
            return Matcher.quoteReplacement(greek ? name : "");
        });
        s = ESCAPED_SYMBOL.matcher(s).replaceAll("");
        s = MARKUP.matcher(s).replaceAll("");
        // NFKD turns ₂ into 2 and ⁺ into +, and splits accents off their base letters.
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);
        s = COMBINING_MARKS.matcher(s).replaceAll("");
        s = s.toLowerCase(Locale.ROOT).replace('ł', 'l');
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            String greekName = GREEK.get(c);
            if (greekName != null) out.append(greekName); else out.append(c);
        }
        return WHITESPACE.matcher(out).replaceAll("");
    }
}
