package com.anteater.memberservice.member.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio,

        String profileImage
) {}