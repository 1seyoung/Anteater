package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResendActivationResponse {
    private final String message;
    private final boolean emailSent;
}

// 계정 활성화 이메일 재전송 결과를 나타내는 읽기 전용 응답