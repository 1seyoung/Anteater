package com.anteater.memberservice.auth.dto;


public record LoginResponse(
        AuthenticationResult authResult,
        String refreshToken
) {}