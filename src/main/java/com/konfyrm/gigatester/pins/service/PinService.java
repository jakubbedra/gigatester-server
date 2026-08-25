package com.konfyrm.gigatester.pins.service;

import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.domain.dto.PinResponse;
import com.konfyrm.gigatester.pins.domain.entity.UserPin;
import com.konfyrm.gigatester.pins.repository.UserPinRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PinService {

    private final UserPinRepository userPinRepository;

    public List<PinResponse> getPins(User user) {
        return userPinRepository.findByUser_IdOrderByPinnedAtDesc(user.getId()).stream()
                .map(p -> PinResponse.builder().entityType(p.getEntityType()).entityId(p.getEntityId()).build())
                .toList();
    }

    public void pin(User user, PinnedEntityType entityType, UUID entityId) {
        if (userPinRepository.findByUser_IdAndEntityTypeAndEntityId(user.getId(), entityType, entityId).isPresent()) {
            return;
        }
        userPinRepository.save(UserPin.builder()
                .user(user)
                .entityType(entityType)
                .entityId(entityId)
                .build());
    }

    @Transactional
    public void unpin(User user, PinnedEntityType entityType, UUID entityId) {
        userPinRepository.deleteByUser_IdAndEntityTypeAndEntityId(user.getId(), entityType, entityId);
    }
}
