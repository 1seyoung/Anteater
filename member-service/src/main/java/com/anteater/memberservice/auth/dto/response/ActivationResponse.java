package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActivationResponse {
    private final String message;
    private final String userId;
    private final String subscriptionStatus;
}

//계정 활성화 결과를 나타내는 읽기 전용 응답