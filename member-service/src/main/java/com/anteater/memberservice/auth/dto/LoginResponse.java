package com.anteater.memberservice.auth.dto;


import com.anteater.memberservice.entity.SubscriptionStatus;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String username,
        SubscriptionStatus subscriptionStatus
) {}