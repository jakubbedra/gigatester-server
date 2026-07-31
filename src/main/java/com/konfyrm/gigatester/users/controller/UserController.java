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

    @PutMapping("/me/bio")
    ResponseEntity<UserResponse> updateBio(@AuthenticationPrincipal User user,
                                           @RequestBody java.util.Map<String, String> body);

    @GetMapping
    ResponseEntity<List<UserResponse>> findAll();

    @GetMapping("/{id}")
    ResponseEntity<UserResponse> findById(@PathVariable UUID id);

    @PutMapping("/{id}/role")
    ResponseEntity<UserResponse> assignRole(@PathVariable UUID id, @RequestBody java.util.Map<String, UUID> body);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);

    @PostMapping("/{id}/password-reset-token")
    ResponseEntity<String> generatePasswordResetToken(@PathVariable UUID id);
}
