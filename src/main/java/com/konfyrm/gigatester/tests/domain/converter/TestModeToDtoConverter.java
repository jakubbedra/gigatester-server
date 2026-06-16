package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.enums.TestModeDto;
import com.konfyrm.gigatester.tests.domain.entity.TestMode;

import java.util.Map;

public class TestModeToDtoConverter {

    private static final Map<TestMode, TestModeDto> ENTITY_TO_DTO_MAP = Map.of(
            TestMode.EXAM, TestModeDto.EXAM,
            TestMode.LEARNING, TestModeDto.LEARNING
    );

    private static final Map<TestModeDto, TestMode> DTO_TO_ENTITY_MAP = Map.of(
            TestModeDto.EXAM, TestMode.EXAM,
            TestModeDto.LEARNING, TestMode.LEARNING
    );

    private TestModeToDtoConverter() { }

    public static TestMode toEntity(TestModeDto dto) {
        return DTO_TO_ENTITY_MAP.get(dto);
    }

    public static TestModeDto toDto(TestMode entity) {
        return ENTITY_TO_DTO_MAP.get(entity);
    }

}