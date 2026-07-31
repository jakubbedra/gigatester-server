package com.konfyrm.gigatester.security.controller;

import com.konfyrm.gigatester.security.domain.dto.RoleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/roles")
public interface RoleController {

    @GetMapping
    ResponseEntity<?> getRoles();

    @PostMapping
    ResponseEntity<?> createRole(@RequestBody RoleRequest request);

    @PutMapping("/{id}")
    ResponseEntity<?> updateRole(@PathVariable("id") UUID id, @RequestBody RoleRequest request);

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteRole(@PathVariable("id") UUID id);

}
