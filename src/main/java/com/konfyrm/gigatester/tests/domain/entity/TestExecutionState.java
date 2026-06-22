package com.konfyrm.gigatester.tests.domain.entity;

import java.util.Map;

public enum TestExecutionState {
    NOT_STARTED,
    IN_PROGRESS,
    IN_REVIEW,
    FINISHED;

    private static final Map<TestExecutionState, TestExecutionState> NEXT_STATE = Map.of(
            NOT_STARTED, IN_PROGRESS,
            IN_PROGRESS, FINISHED,
            FINISHED, NOT_STARTED
    );

    public static TestExecutionState getNext(TestExecutionState state) {
        return NEXT_STATE.get(state);
    }

}