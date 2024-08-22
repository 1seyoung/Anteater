package com.anteater.memberservice.member.dto.response;

import java.time.LocalDateTime;

public record ProfileResponse(
        String username,
        String email,
        String bio,
        String profileImage,
        boolean isSubscribed,  // SubscriptionStatus에서 boolean으로 변경
        LocalDateTime updatedAt) {
    // 생성자는 컴파일러가 자동으로 생성합니다.
}