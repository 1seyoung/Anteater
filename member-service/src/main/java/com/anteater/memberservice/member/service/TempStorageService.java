package com.anteater.memberservice.member.service;

import com.anteater.memberservice.member.dto.request.RegisterRequest;

public interface TempStorageService {
    void saveRegistrationInfo(String token, RegisterRequest request);
    RegisterRequest getRegistrationInfo(String token);
    void removeRegistrationInfo(String token);
}