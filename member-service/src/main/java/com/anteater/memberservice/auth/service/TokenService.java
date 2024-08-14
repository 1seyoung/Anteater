package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.AuthenticationResult;
import com.anteater.memberservice.auth.dto.LoginRequest;
import com.anteater.memberservice.auth.dto.LoginResponse;
import com.anteater.memberservice.config.JwtConfig;
import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.entity.SubscriptionStatus;
import com.anteater.memberservice.exception.InvalidTokenException;
import com.anteater.memberservice.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenService(MemberRepository memberRepository,
                        PasswordEncoder passwordEncoder,
                        RedisTemplate<String, Object> redisTemplate) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    public AuthenticationResult authenticate(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new AuthenticationResult(
                member.getId(),
                member.getUsername(),
                member.getSubscriptionStatus()
        );
    }

    public void storeRefreshToken(Long userId, String refreshToken, long expirationTime) {
        redisTemplate.opsForValue().set(
                getRefreshTokenKey(userId),
                refreshToken,
                expirationTime,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean validateRefreshToken(Long userId, String token) {
        String storedToken = (String) redisTemplate.opsForValue().get(getRefreshTokenKey(userId));
        return token.equals(storedToken);
    }

    public void revokeRefreshToken(Long userId) {
        redisTemplate.delete(getRefreshTokenKey(userId));
    }

    public void logoutFromAllDevices(Long userId) {
        redisTemplate.delete(getRefreshTokenKey(userId));
    }

    private String getRefreshTokenKey(Long userId) {
        return "refresh_token:" + userId;
    }
}