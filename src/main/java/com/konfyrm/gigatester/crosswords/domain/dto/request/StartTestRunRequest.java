package com.konfyrm.gigatester.crosswords.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class StartTestRunRequest {
    private String mode; // "ALL" or "WRONG_ONLY"

    /** True to resume an in-progress run of the same mode if one exists; false always starts fresh, discarding it. */
    private boolean resume;

    /** Optional: restrict the pool to terms carrying at least one (or all, see matchAllTags) of these tags. */
    private List<UUID> tagIds;

    /** true = term must have ALL tagIds; false = at least one. Ignored when tagIds is empty. */
    private boolean matchAllTags;

    /** true = exclude terms matching tagIds instead of restricting to them. Ignored when tagIds is empty. */
    private boolean excludeTags;
}
