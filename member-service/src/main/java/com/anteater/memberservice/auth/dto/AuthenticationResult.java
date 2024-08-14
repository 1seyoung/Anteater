package com.anteater.memberservice.auth.dto;

import com.anteater.memberservice.entity.SubscriptionStatus;

public record AuthenticationResult(
        Long userId,
        String username,
        SubscriptionStatus subscriptionStatus
) {}