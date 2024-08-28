package com.anteater.memberservice.common.redis;

import com.anteater.memberservice.member.dto.request.RegisterRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


public class RedisTempStorageService<T> implements TempStorageService<T> {
    private final RedisTemplate<String, T> redisTemplate;
    private final String keyPrefix;

    public RedisTempStorageService(RedisTemplate<String, T> redisTemplate, String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(String key, T value, long expirationTime) {
        redisTemplate.opsForValue().set(keyPrefix + key, value, expirationTime, TimeUnit.SECONDS);
    }

    @Override
    public T get(String key) {
        return redisTemplate.opsForValue().get(keyPrefix + key);
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(keyPrefix + key);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(keyPrefix + key));
    }
}