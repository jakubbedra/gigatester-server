package com.konfyrm.gigatester.graphs.controller;

import com.konfyrm.gigatester.graphs.domain.dto.request.GraphCollectionRequest;
import com.konfyrm.gigatester.graphs.domain.entity.GraphCollection;
import com.konfyrm.gigatester.graphs.service.GraphCollectionService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.users.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GraphCollectionControllerImpl implements GraphCollectionController {

    private final GraphCollectionService collectionService;
    private final PermissionService permissionService;

    private void requireWriter(User user) {
        permissionService.require(permissionService.canCreate(user, Permission.GRAPHS_WRITE) || permissionService.isStaff(user));
    }

    @Override
    public ResponseEntity<?> list(User user) {
        permissionService.require(permissionService.isStaff(user));
        return ResponseEntity.ok(collectionService.listAll());
    }

    @Override
    public ResponseEntity<?> playable(User user) {
        return ResponseEntity.ok(collectionService.playable(user));
    }

    @Override
    public ResponseEntity<?> create(GraphCollectionRequest request, User user) {
        requireWriter(user);
        GraphCollection saved = collectionService.create(request, user);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> get(UUID id, User user) {
        permissionService.require(permissionService.isStaff(user));
        return ResponseEntity.ok(collectionService.get(id));
    }

    @Override
    public ResponseEntity<?> play(UUID id, User user) {
        return ResponseEntity.ok(collectionService.play(id, user));
    }

    @Override
    public ResponseEntity<?> update(UUID id, GraphCollectionRequest request, User user) {
        requireWriter(user);
        collectionService.update(id, request);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> delete(UUID id, User user) {
        requireWriter(user);
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
