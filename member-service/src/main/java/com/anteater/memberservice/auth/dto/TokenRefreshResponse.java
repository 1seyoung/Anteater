package com.anteater.memberservice.auth.dto;

public record TokenRefreshResponse(String accessToken, String refreshToken) {}