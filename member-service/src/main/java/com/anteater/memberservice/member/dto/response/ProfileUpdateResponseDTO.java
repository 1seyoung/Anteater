package com.anteater.memberservice.member.dto.response;

import java.time.LocalDateTime;

public record ProfileUpdateResponseDTO (
        String username,
        String displayName,
        String email,
        String bio,
        String profileImage,
        LocalDateTime updatedAt) {
}
