package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.enums.TestExecutionStateDto;
import com.konfyrm.gigatester.tests.domain.entity.TestExecutionState;

import java.util.Map;

public class TestExecutionStateToDtoConverter {

    private static final Map<TestExecutionState, TestExecutionStateDto> ENTITY_TO_DTO_MAP = Map.of(
            TestExecutionState.NOT_STARTED, TestExecutionStateDto.NOT_STARTED,
            TestExecutionState.IN_PROGRESS, TestExecutionStateDto.IN_PROGRESS,
            TestExecutionState.FINISHED, TestExecutionStateDto.FINISHED
    );

    private static final Map<TestExecutionStateDto, TestExecutionState> DTO_TO_ENTITY_MAP = Map.of(
            TestExecutionStateDto.NOT_STARTED, TestExecutionState.NOT_STARTED,
            TestExecutionStateDto.IN_PROGRESS, TestExecutionState.IN_PROGRESS,
            TestExecutionStateDto.FINISHED, TestExecutionState.FINISHED
    );

    private TestExecutionStateToDtoConverter() { }

    public static TestExecutionState toEntity(TestExecutionStateDto dto) {
        return DTO_TO_ENTITY_MAP.get(dto);
    }

    public static TestExecutionStateDto toDto(TestExecutionState entity) {
        return ENTITY_TO_DTO_MAP.get(entity);
    }

}