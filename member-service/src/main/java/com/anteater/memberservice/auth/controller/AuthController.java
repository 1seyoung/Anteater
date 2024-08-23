package com.anteater.memberservice.auth.controller;



import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.auth.service.AuthService;
import com.anteater.memberservice.auth.service.TokenService;
import com.anteater.memberservice.common.exception.InvalidRefreshTokenException;
import com.anteater.memberservice.common.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, TokenService tokenService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        AuthenticationResult authResult = authService.authenticate(request);
        String refreshToken = tokenService.getRefreshToken(authResult.userId());
        return ResponseEntity.ok(new AuthenticationResponse(authResult, refreshToken));
    }

    // 토큰 갱신(refresh) 엔드포인트
    // 목적 : 만료된 액세스 토큰을 새로운 액세스 토큰으로 교체
    // 결과 : 새로운 엑세스 토큰(때로는 새로운 리프레시 토큰도 반환)


    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();
        try {
            TokenPair newTokenPair = tokenService.refreshToken(refreshToken);
            return ResponseEntity.ok(new TokenRefreshResponse(newTokenPair.accessToken(), newTokenPair.refreshToken()));
        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // 08.22
    // 목적 : 주어진 토큰(주로 액세스 토큰)이 유효한지 확인
    // 동작 : 토큰의 서명, 만료 여부 등을 검사
    // 결과 : 토큰의 유효성 여부만 반환(유효하면 true, 그렇지 않으면 false)
    // 주 사용처 : API Gateway나 다른 서비스에서 토큰의 유효성을 빠르게 확인할 때 사용

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        boolean isValid = jwtUtil.validateToken(request.token());
        return ResponseEntity.ok(new TokenValidationResponse(isValid));
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