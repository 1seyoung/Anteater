package com.anteater.memberservice.auth.controller;



import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResult> login(@RequestBody LoginRequest request) {
        AuthenticationResult result = authService.authenticate(request);
        return ResponseEntity.ok(result);
    }
}