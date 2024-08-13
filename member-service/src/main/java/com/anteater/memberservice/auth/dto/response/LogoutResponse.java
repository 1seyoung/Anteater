package com.anteater.memberservice.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public record LogoutResponse(String message) {
}
// 로그아웃 결과를 나타내는 읽기 전용 응답
