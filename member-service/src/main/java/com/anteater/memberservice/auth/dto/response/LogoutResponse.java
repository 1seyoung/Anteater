package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LogoutResponse {
    private final String message;
}
// 로그아웃 결과를 나타내는 읽기 전용 응답
// @RequiredArgsConstructor를 사용하여 모든 필드를 초기화하는 생성자 만듬