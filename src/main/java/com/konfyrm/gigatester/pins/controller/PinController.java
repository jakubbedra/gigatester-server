package com.konfyrm.gigatester.pins.controller;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.domain.dto.PinRequest;
import com.konfyrm.gigatester.pins.domain.dto.PinResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/pins")
public interface PinController {

    @GetMapping
    ResponseEntity<List<PinResponse>> getPins(@AuthenticationPrincipal User user);

    @PostMapping
    ResponseEntity<Void> pin(@AuthenticationPrincipal User user, @RequestBody PinRequest request);

    @DeleteMapping("/{entityType}/{entityId}")
    ResponseEntity<Void> unpin(@AuthenticationPrincipal User user,
                               @PathVariable PinnedEntityType entityType,
                               @PathVariable UUID entityId);
}
