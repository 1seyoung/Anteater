package com.anteater.memberservice.auth.service;

import com.anteater.memberservice.auth.dto.AuthenticationResult;
import com.anteater.memberservice.entity.Member;
import com.anteater.memberservice.repository.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    public TokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public void storeRefreshToken(Long userId, String refreshToken, long expirationTime) {
        redisTemplate.opsForValue().set(
                getRefreshTokenKey(userId),
                refreshToken,
                expirationTime,
                TimeUnit.MILLISECONDS
        );
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(getRefreshTokenKey(userId));
    }

    public void revokeRefreshToken(Long userId) {
        redisTemplate.delete(getRefreshTokenKey(userId));
    }


    private String getRefreshTokenKey(Long userId) {
        return "refresh_token:" + userId + ":" + UUID.randomUUID().toString();
    }


    // 로그아웃 관련
    public void logout(Long userId) {
        String key = getRefreshTokenKey(userId);
        redisTemplate.delete(key);
    }
    public void logoutAllDevices(Long userId) {
        Set<String> keys = redisTemplate.keys("refresh_token:" + userId + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

}