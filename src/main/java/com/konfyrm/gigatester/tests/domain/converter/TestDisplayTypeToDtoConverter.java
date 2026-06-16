package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.tests.domain.dto.enums.TestDisplayTypeDto;
import com.konfyrm.gigatester.tests.domain.entity.TestDisplayType;

import java.util.Map;

public class TestDisplayTypeToDtoConverter {

    private static final Map<TestDisplayType, TestDisplayTypeDto> TEST_DISPLAY_TYPE_TO_DTO_MAP = Map.of(
            TestDisplayType.ALL_AT_ONCE, TestDisplayTypeDto.ALL_AT_ONCE,
            TestDisplayType.ONE_BY_ONE, TestDisplayTypeDto.ONE_BY_ONE
    );

    private static final Map<TestDisplayTypeDto, TestDisplayType> TEST_DISPLAY_TYPE_DTO_TO_ENTITY_MAP = Map.of(
            TestDisplayTypeDto.ALL_AT_ONCE, TestDisplayType.ALL_AT_ONCE,
            TestDisplayTypeDto.ONE_BY_ONE, TestDisplayType.ONE_BY_ONE
    );

    private TestDisplayTypeToDtoConverter() { }

    public static TestDisplayType toEntity(TestDisplayTypeDto dto) {
        return TEST_DISPLAY_TYPE_DTO_TO_ENTITY_MAP.get(dto);
    }

    public static TestDisplayTypeDto toDto(TestDisplayType entity) {
        return TEST_DISPLAY_TYPE_TO_DTO_MAP.get(entity);
    }

}