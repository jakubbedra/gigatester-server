package com.konfyrm.gigatester.users.controller;

import com.konfyrm.gigatester.questions.service.ImageService;
import com.konfyrm.gigatester.questions.service.impl.R2ImageService;
import com.konfyrm.gigatester.users.domain.dto.request.ChangePasswordRequest;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;
    @Qualifier(R2ImageService.QUALIFIER)
    private final ImageService imageService;

    @Override
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMe(user));
    }

    @Override
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                               ChangePasswordRequest request) {
        userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserResponse> uploadProfilePicture(@AuthenticationPrincipal User user,
                                                             MultipartFile file) {
        try {
            String key = "users/" + user.getId() + "/avatar";
            String url = imageService.uploadWithKey(key, file);
            return ResponseEntity.ok(userService.updateProfilePicture(user, url));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Override
    public ResponseEntity<UserResponse> findById(UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> promote(UUID id) {
        return ResponseEntity.ok(userService.promoteToModerator(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> demote(UUID id) {
        return ResponseEntity.ok(userService.demoteToUser(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> generatePasswordResetToken(UUID id) {
        return ResponseEntity.ok(userService.generatePasswordResetToken(id));
    }
}
