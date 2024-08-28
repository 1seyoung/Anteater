package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.LoginResponseDto;
import com.anteater.memberservice.common.config.JwtConfig;

import com.anteater.memberservice.common.redis.RedisTempStorageService;

import com.anteater.memberservice.common.repository.MemberRepository;
import com.anteater.memberservice.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class TokenService {

    //final 을 왜 쓰는건지 생각해볼 것
    private static final String REFRESH_TOKEN_KEY = "refresh:user:%s";
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7일

    private final JwtUtil jwtUtil;
    private final RedisTempStorageService<String> redisTempStorageService;


    //생성자를 꼭 정의해야하는 이유? 항상?
    public TokenService(JwtUtil jwtUtil, @Qualifier("refreshTokenStorageService") RedisTempStorageService<String> redisTempStorageService) {
        this.jwtUtil = jwtUtil;
        this.redisTempStorageService = redisTempStorageService;
    }


    // TODO : (login)로그인 정보 통과하면 AccessToken, RefreshToken을 생성하는 메소드 -> AccessToken은 클라이언트로 반환 / RefreshToken은 Redis에 저장

    public LoginResponseDto generateTokenPair(String username) {
        String accessToken = jwtUtil.generateToken(username);
        generateAndStoreRefreshToken(username);  // Refresh Token은 서버에만 저장
        return new LoginResponseDto(accessToken);
    }

    private void generateAndStoreRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        String key = String.format(REFRESH_TOKEN_KEY, username);
        redisTempStorageService.save(key, refreshToken, REFRESH_TOKEN_VALIDITY);
    }


    // (refresh)일단 만료된 토큰이 오면 서명 검증하고 서명 맞으면 새로운 AccessToken 발급을 위해 RefreshToken을 조회 있으면 -> 있으면 AccessToken 발급 없으면 다시 client에게 로그인 요청
    public String refreshAccessToken(String username) {

        String key = String.format(REFRESH_TOKEN_KEY, username);

        // 레디스에서 리프레시 토큰 userid 기준으로 조회 있으면 true 없으면 false
        if(redisTempStorageService.exists(key)) {
            // 리프레시 토큰을 이용해서 새로운 AccessToken 발급
            return jwtUtil.generateToken(username);

        } else {
            //값이 없다 -> RefreshToken이 만료되었거나 유효하지 않은 토큰 -> 다시 로그인 요청
            throw new RuntimeException("Refresh token has expired or is invalid. Please log in again.");
        }


    }


    // (logout)로그아웃 요청이 오면 RefreshToken을 삭제
    public void revokeRefreshToken(String username) {
        String key = String.format(REFRESH_TOKEN_KEY, username);
        System.out.println("삭제");
        redisTempStorageService.remove(key);
    }


}