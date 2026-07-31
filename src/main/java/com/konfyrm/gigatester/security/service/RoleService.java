package com.konfyrm.gigatester.security.service;

import com.konfyrm.gigatester.security.domain.Role;
import com.konfyrm.gigatester.security.domain.dto.RoleRequest;
import com.konfyrm.gigatester.security.domain.dto.RoleResponse;
import com.konfyrm.gigatester.security.repository.RoleRepository;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RoleResponse create(RoleRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
        }
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A role named '" + request.getName() + "' already exists");
        }
        Role role = Role.builder()
                .name(request.getName())
                .permissions(request.getPermissions() == null ? new HashSet<>() : new HashSet<>(request.getPermissions()))
                .build();
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse update(UUID roleId, RoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        if (request.getName() != null && !request.getName().isBlank()) {
            role.setName(request.getName());
        }
        if (request.getPermissions() != null) {
            role.setPermissions(new HashSet<>(request.getPermissions()));
        }
        return toResponse(roleRepository.save(role));
    }

    public void delete(UUID roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        userRepository.findAll().stream()
                .filter(u -> u.getAssignedRole() != null && u.getAssignedRole().getId().equals(roleId))
                .forEach(u -> {
                    u.setAssignedRole(null);
                    userRepository.save(u);
                });
        roleRepository.deleteById(roleId);
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(role.getPermissions())
                .build();
    }

}
