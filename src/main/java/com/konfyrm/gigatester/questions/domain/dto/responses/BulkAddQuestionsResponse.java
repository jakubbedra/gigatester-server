package com.konfyrm.gigatester.questions.domain.dto.responses;

import lombok.*;

import java.util.List;
import java.util.UUID;

/** Result of a bulk question import: every question is attempted, so a single bad entry doesn't abort the rest. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAddQuestionsResponse {

    private List<UUID> createdIds;
    private List<Failure> failures;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Failure {
        /** Index of the failing question within the request's "questions" array. */
        private int index;
        private String message;
    }

}
