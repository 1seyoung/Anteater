package com.anteater.memberservice.auth.dto;

public record AuthenticationResponse(
        AuthenticationResult authResult,
        String refreshToken
) {}