package com.anteater.memberservice.auth.controller;



import com.anteater.memberservice.auth.dto.request.LoginRequest;
import com.anteater.memberservice.auth.dto.request.LogoutRequest;
import com.anteater.memberservice.auth.dto.request.RegisterRequest;
import com.anteater.memberservice.auth.dto.request.ResendActivationRequest;
import com.anteater.memberservice.auth.dto.response.*;
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


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activate")
    public ResponseEntity<ActivationResponse> activateAccount(@RequestParam String token) {
        ActivationResponse response = authService.activateAccount(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        LogoutResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutFromAllDevices(@RequestHeader("X-Authenticated-User") String email) {
        Long userId = authService.getUserIdByEmail(email);
        if (userId == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        tokenService.logoutFromAllDevices(userId);
        return ResponseEntity.ok("Logged out from all devices successfully.");
    }

    // 필요한 경우, 이메일 재전송을 위한 엔드포인트 추가
    @PostMapping("/resend-activation")
    public ResponseEntity<ResendActivationResponse> resendActivation(@RequestBody ResendActivationRequest request) {
        ResendActivationResponse response = authService.resendActivationEmail(request.getEmail());
        return ResponseEntity.ok(response);
    }
}