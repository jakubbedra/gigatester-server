package com.konfyrm.gigatester.graphs.controller;

import com.konfyrm.gigatester.graphs.domain.converter.GraphConverter;
import com.konfyrm.gigatester.graphs.domain.dto.request.GraphRequest;
import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import com.konfyrm.gigatester.graphs.service.GraphService;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GraphControllerImpl implements GraphController {

    private final GraphService graphService;
    private final GraphConverter graphConverter;
    private final PermissionService permissionService;

    private final SubjectGroupAccessService accessService;

    public GraphControllerImpl(GraphService graphService, GraphConverter graphConverter,
                               PermissionService permissionService, SubjectGroupAccessService accessService) {
        this.graphService = graphService;
        this.graphConverter = graphConverter;
        this.permissionService = permissionService;
        this.accessService = accessService;
    }

    @Override
    public ResponseEntity<?> addGraph(GraphRequest request, User user) {
        permissionService.require(permissionService.canCreate(user, Permission.GRAPHS_WRITE));
        Graph entity = graphConverter.toEntity(request);
        entity.setCreatedBy(user);
        Graph saved = graphService.addGraph(entity);
        return ResponseEntity.accepted().body(saved.getId());
    }

    @Override
    public ResponseEntity<?> getGraphs(User user) {
        permissionService.require(permissionService.isStaff(user));
        return ResponseEntity.ok(graphConverter.toSummaryResponses(graphService.findGraphs()));
    }

    @Override
    public ResponseEntity<?> getPlayableGraphs(User user) {
        return ResponseEntity.ok(graphConverter.toSummaryResponses(
                graphService.findGraphs().stream()
                        .filter(g -> accessService.hasAccessToGraph(g.getId(), user))
                        .toList()));
    }

    @Override
    public ResponseEntity<?> getGraph(UUID id, User user) {
        permissionService.require(permissionService.hasGraphPermission(user, id, Permission.GRAPHS_WRITE));
        return ResponseEntity.ok(graphConverter.toResponse(graphService.findGraph(id)));
    }

    @Override
    public ResponseEntity<?> updateGraph(UUID id, GraphRequest request, User user) {
        permissionService.require(permissionService.hasGraphPermission(user, id, Permission.GRAPHS_WRITE));
        Graph entity = graphConverter.toEntity(request);
        graphService.updateGraph(id, entity);
        return ResponseEntity.accepted().body(id);
    }

    @Override
    public ResponseEntity<?> deleteGraph(UUID id, User user) {
        permissionService.require(permissionService.hasGraphPermission(user, id, Permission.GRAPHS_WRITE));
        graphService.deleteGraph(id);
        return ResponseEntity.noContent().build();
    }

}
