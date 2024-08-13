package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public record ResendActivationResponse(String message, boolean emailSent) {
}

// 계정 활성화 이메일 재전송 결과를 나타내는 읽기 전용 응답