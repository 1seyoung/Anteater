package com.anteater.memberservice.member.dto.response;


public record RegisterResponse(
        String message,
        String userId,
        boolean isSubscribed,
        boolean isEnabled,
        boolean activationEmailSent
) {}

