package com.konfyrm.gigatester.users.controller;

import com.konfyrm.gigatester.users.domain.dto.request.LoginRequest;
import com.konfyrm.gigatester.users.domain.dto.request.RegisterRequest;
import com.konfyrm.gigatester.users.domain.dto.response.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/auth")
public interface AuthController {

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request);

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request);

    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword);

}