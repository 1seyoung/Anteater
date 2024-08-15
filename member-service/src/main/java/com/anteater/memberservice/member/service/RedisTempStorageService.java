package com.anteater.memberservice.member.service;

import com.anteater.memberservice.member.dto.request.RegisterRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTempStorageService implements TempStorageService {
    private final RedisTemplate<String, RegisterRequest> redisTemplate;
    private final long EXPIRATION_TIME = 24 * 60 * 60; // 24시간 (초 단위)

    public RedisTempStorageService(RedisTemplate<String, RegisterRequest> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveRegistrationInfo(String token, RegisterRequest request) {
        redisTemplate.opsForValue().set(token, request, EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    @Override
    public RegisterRequest getRegistrationInfo(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    @Override
    public void removeRegistrationInfo(String token) {
        redisTemplate.delete(token);
    }
}