package com.anteater.memberservice.member.dto.request;

public record ResendActivationRequest(String email) {
}

// 회원가입 이메일 인증 재전송 요청을 나타내는 불변 객체