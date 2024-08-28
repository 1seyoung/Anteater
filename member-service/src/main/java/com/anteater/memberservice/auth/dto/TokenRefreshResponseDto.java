package com.anteater.memberservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDto {
    private String accessToken;
    private String error;

    public static TokenRefreshResponseDto success(String accessToken) {
        return new TokenRefreshResponseDto(accessToken, null);
    }

    public static TokenRefreshResponseDto error(String errorMessage) {
        return new TokenRefreshResponseDto(null, errorMessage);
    }
}