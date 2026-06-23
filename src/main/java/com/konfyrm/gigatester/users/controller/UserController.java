package com.konfyrm.gigatester.users.controller;

import com.konfyrm.gigatester.users.domain.dto.request.ChangePasswordRequest;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/users")
public interface UserController {

    @GetMapping("/me")
    ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user);

    @PutMapping("/me/password")
    ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                        @RequestBody ChangePasswordRequest request);

    @PostMapping("/me/profile-picture")
    ResponseEntity<UserResponse> uploadProfilePicture(@AuthenticationPrincipal User user,
                                                      @RequestParam("file") MultipartFile file);

    @GetMapping
    ResponseEntity<List<UserResponse>> findAll();

    @GetMapping("/{id}")
    ResponseEntity<UserResponse> findById(@PathVariable UUID id);

    @PutMapping("/{id}/promote")
    ResponseEntity<UserResponse> promote(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
