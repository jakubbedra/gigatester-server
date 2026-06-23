package com.konfyrm.gigatester.tags.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TagResponse {
    private UUID id;
    private String key;
}
