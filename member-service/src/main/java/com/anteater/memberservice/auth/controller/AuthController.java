package com.anteater.memberservice.auth.controller;



import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.auth.service.AuthService;
import com.anteater.memberservice.auth.service.TokenService;
import com.anteater.memberservice.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    /*
    api : /auth/login
    목적 : 사용자 로그인
    요청 : POST
    요청 바디 : 로그인 정보
    응답 : 로그인 성공 시 토큰 반환
    로직 : 사용자의 자격 증명을 검증한 후, 인증 성공 시 JWT 토큰 쌍(액세스 토큰, 리프레시 토큰)을 생성하여 반환
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        LoginResponseDto loginResponseDto = authService.authenticate(request);
        return ResponseEntity.ok(loginResponseDto);
    }

    //ACCESS TOKEN 이 만료되었을 때 아직 리프레시 토큰이 유효한지 확인하는 로직
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("X-Auth-Username") String username) {
        try {
            String newAccessToken = tokenService.refreshAccessToken(username);
            return ResponseEntity.ok(TokenRefreshResponseDto.success(newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenRefreshResponseDto.error(e.getMessage()));
        }
    }

    // Log out
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequestDto request) {
        tokenService.revokeRefreshToken(request.getUsername());
        System.out.println("logout:QQQ:"+request.getUsername());
        return ResponseEntity.ok().build();
    }

}