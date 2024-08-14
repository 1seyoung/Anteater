package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.*;
import com.anteater.memberservice.entity.SubscriptionStatus;
import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.exception.*;
import com.anteater.memberservice.auth.repository.AuthRepository;
import com.anteater.memberservice.member.service.EmailService;
import com.anteater.memberservice.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticationResult authenticate(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new AuthenticationResult(
                member.getId(),
                member.getUsername(),
                member.getSubscriptionStatus()
        );
    }
}
