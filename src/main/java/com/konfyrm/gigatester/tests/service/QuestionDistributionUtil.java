package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.tests.domain.dto.enums.TestQuestionDistributionMode;

import java.util.*;

/**
 * Caps a candidate question pool to at most N questions per selected tag,
 * so a test doesn't end up dominated by whichever tag happens to have the
 * most questions. Only meaningful in INCLUDE mode with at least one tag
 * selected; otherwise the pool is returned unchanged.
 */
public final class QuestionDistributionUtil {

    private QuestionDistributionUtil() {}

    public static List<Question> apply(List<Question> pool, List<UUID> tagIds, boolean exclude,
                                        TestQuestionDistributionMode mode, int maxPerTag) {
        if (mode != TestQuestionDistributionMode.MAX_PER_TAG || exclude
                || tagIds == null || tagIds.isEmpty() || maxPerTag <= 0) {
            return pool;
        }

        List<UUID> shuffledTags = new ArrayList<>(tagIds);
        Collections.shuffle(shuffledTags);

        Set<UUID> pickedIds = new HashSet<>();
        List<Question> picked = new ArrayList<>();

        for (UUID tagId : shuffledTags) {
            int added = 0;
            for (Question q : pool) {
                if (added >= maxPerTag) break;
                if (pickedIds.contains(q.getId())) continue;
                boolean hasTag = q.getTags() != null && q.getTags().stream().anyMatch(t -> t.getId().equals(tagId));
                if (!hasTag) continue;
                picked.add(q);
                pickedIds.add(q.getId());
                added++;
            }
        }

        Collections.shuffle(picked);
        return picked;
    }
}
