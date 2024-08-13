package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public record RegisterResponse(String message, String userId, String subscriptionStatus, boolean activationEmailSent) {
}
//읽기 전용 속성만 있으므로 getter만 제공
//@RequiredArgsConstructor를 사용하여 모든 필드를 초기화하는 생성자 만듬