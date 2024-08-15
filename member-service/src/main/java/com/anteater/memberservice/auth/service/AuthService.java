package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.repository.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;

/**
 * AuthService는 사용자 인증과 관련된 로직을 처리하는 서비스 클래스입니다.
 * 사용자 등록, 로그인, 토큰 갱신 등의 기능을 제공합니다.
 */
// AuthService.java
@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthenticationResult authenticate(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        AuthenticationResult result = new AuthenticationResult(
                member.getId(),
                member.getUsername(),
                member.isSubscribed(),
                new HashSet<>(member.getRoles())
        );

        String refreshToken = tokenService.generateRefreshToken();
        long expirationTime = 7 * 24 * 60 * 60 * 1000; // 7일
        tokenService.storeRefreshToken(member.getId(), refreshToken, expirationTime);

        return result;
    }
}

