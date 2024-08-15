package com.anteater.memberservice.auth.controller;



import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.auth.service.AuthService;
import com.anteater.memberservice.auth.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        AuthenticationResult authResult = authService.authenticate(request);
        String refreshToken = tokenService.getRefreshToken(authResult.userId());
        return ResponseEntity.ok(new AuthenticationResponse(authResult, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        tokenService.revokeRefreshToken(request.userId());
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/logout-all")
//    public ResponseEntity<Void> logoutAllDevices(@RequestBody LogoutRequest request) {
//        authService.logoutAllDevices(request.userId());
//        return ResponseEntity.ok().build();
//    }
}