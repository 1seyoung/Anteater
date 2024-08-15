package com.anteater.memberservice.auth.dto;

import java.util.Set;

public record AuthenticationResult(
        Long userId,
        String username,
        boolean isSubscribed,
        Set<String> roles
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}