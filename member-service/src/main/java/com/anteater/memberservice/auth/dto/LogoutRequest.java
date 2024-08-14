package com.anteater.memberservice.auth.dto;


public record LogoutRequest(Long userId, boolean allDevices, String refreshToken) {
}
//단일 속성을 가진 불변 객체로 만들어 로그아웃 요청을 나타냄