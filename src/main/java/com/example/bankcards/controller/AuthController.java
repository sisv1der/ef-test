package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.username().isEmpty()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (loginRequest.password().isEmpty()) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }
}
