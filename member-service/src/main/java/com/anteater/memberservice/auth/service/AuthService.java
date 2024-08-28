package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.common.entity.Member;
import com.anteater.memberservice.common.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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



    public LoginResponseDto authenticate(LoginRequestDto request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid Username"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        return tokenService.generateTokenPair(member.getUsername());
    }

}

