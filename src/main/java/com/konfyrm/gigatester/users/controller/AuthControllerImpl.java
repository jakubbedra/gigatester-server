package com.konfyrm.gigatester.users.controller;

import com.konfyrm.gigatester.users.domain.dto.request.LoginRequest;
import com.konfyrm.gigatester.users.domain.dto.request.RegisterRequest;
import com.konfyrm.gigatester.users.domain.dto.response.AuthResponse;
import com.konfyrm.gigatester.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final UserService userService;

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        return ResponseEntity.ok(userService.login(request.getUsername(), request.getPassword()));
    }
}
