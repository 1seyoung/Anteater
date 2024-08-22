package com.anteater.memberservice.auth.dto;

import java.util.Set;

public record AuthenticationResult(
        Long userId,
        String username,
        boolean isSubscribed,
        String role
) {
    public boolean hasRole(String roleToCheck) {
        return role.equals(roleToCheck);
    }
}