package com.anteater.memberservice.common.redis;


public interface TempStorageService<T> {
    void save(String key, T value, long expirationTime);
    T get(String key);
    void remove(String key);
    boolean exists(String key);
}