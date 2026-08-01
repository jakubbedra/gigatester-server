package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import com.konfyrm.gigatester.tags.entity.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shared tag-filtering logic for crossword term selection, used by both the
 * singleplayer-vs-bot flow ({@code CrosswordStateService}) and the
 * multiplayer flow ({@code CrosswordMultiplayerService}), which otherwise
 * duplicated this exact logic.
 */
public final class CrosswordTagFilter {

    private CrosswordTagFilter() {}

    /**
     * @param tagFilter selected tag ids; null/empty means no filtering
     * @param tagFilterMode "EXCLUDE" to exclude matching terms, anything else (incl. null) to include them
     * @param matchAllTags true = term must have ALL selected tags (AND); false = at least one (OR)
     */
    public static List<CrosswordTerm> filter(List<CrosswordTerm> terms, List<UUID> tagFilter, String tagFilterMode, boolean matchAllTags) {
        if (tagFilter == null || tagFilter.isEmpty()) {
            return terms;
        }
        boolean excludeMode = "EXCLUDE".equalsIgnoreCase(tagFilterMode);
        return terms.stream()
                .filter(t -> {
                    Set<UUID> termTagIds = t.getTags() == null
                            ? Set.of()
                            : t.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
                    boolean matches = matchAllTags
                            ? termTagIds.containsAll(tagFilter)
                            : tagFilter.stream().anyMatch(termTagIds::contains);
                    return excludeMode != matches;
                })
                .collect(Collectors.toList());
    }

}
