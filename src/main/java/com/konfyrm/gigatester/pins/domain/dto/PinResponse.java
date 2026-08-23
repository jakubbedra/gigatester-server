package com.konfyrm.gigatester.pins.domain.dto;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PinResponse {
    private PinnedEntityType entityType;
    private UUID entityId;
}
