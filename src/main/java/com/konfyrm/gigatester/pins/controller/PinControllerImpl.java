package com.konfyrm.gigatester.pins.controller;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.domain.dto.PinRequest;
import com.konfyrm.gigatester.pins.domain.dto.PinResponse;
import com.konfyrm.gigatester.pins.service.PinService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PinControllerImpl implements PinController {

    private final PinService pinService;

    @Override
    public ResponseEntity<List<PinResponse>> getPins(User user) {
        return ResponseEntity.ok(pinService.getPins(user));
    }

    @Override
    public ResponseEntity<Void> pin(User user, PinRequest request) {
        pinService.pin(user, request.getEntityType(), request.getEntityId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unpin(User user, PinnedEntityType entityType, UUID entityId) {
        pinService.unpin(user, entityType, entityId);
        return ResponseEntity.noContent().build();
    }
}
