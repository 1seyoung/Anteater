package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String subscriptionStatus;
}

//읽기 전용 응답으로 구성