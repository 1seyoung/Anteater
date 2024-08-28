package com.anteater.memberservice.member.dto.request;

public record ProfileRequestUpdateDTO(
        String displayName,
        String bio,
        String profileImage
) {}