package com.anteater.memberservice.member.dto.response;


public record ActivationResponse(String message, String userId, boolean subscriptionStatus, String role) {
}


//계정 활성화 결과를 나타내는 읽기 전용 응답