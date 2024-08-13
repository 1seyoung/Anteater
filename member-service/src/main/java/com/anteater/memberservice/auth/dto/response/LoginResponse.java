package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public record LoginResponse(String accessToken, String refreshToken, String subscriptionStatus) {
}

//읽기 전용 응답으로 구성