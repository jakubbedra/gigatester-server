package com.konfyrm.gigatester.pins.domain.dto;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import lombok.Data;

import java.util.UUID;

@Data
public class PinRequest {
    private PinnedEntityType entityType;
    private UUID entityId;
}
