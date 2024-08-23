package com.anteater.memberservice.auth.dto;

public record TokenPair(String accessToken, String refreshToken) {}
