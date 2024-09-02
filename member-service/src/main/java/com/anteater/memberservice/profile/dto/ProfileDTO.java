package com.anteater.memberservice.profile.dto;

public record ProfileDTO(
        String username,
        String email,
        String bio,
        String profileImage,
        boolean isSubscribed
) {
    // 생성자는 record에 의해 자동으로 생성됩니다.
}