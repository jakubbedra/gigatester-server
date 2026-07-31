package com.konfyrm.gigatester.security.controller;

import com.konfyrm.gigatester.security.domain.dto.RoleRequest;
import com.konfyrm.gigatester.security.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoleControllerImpl implements RoleController {

    private final RoleService roleService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRole(RoleRequest request) {
        return ResponseEntity.ok(roleService.create(request));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(UUID id, RoleRequest request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRole(UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
