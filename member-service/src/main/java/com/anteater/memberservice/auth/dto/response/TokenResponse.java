package com.anteater.memberservice.auth.dto.response;

import lombok.Value;

@Value
public class TokenResponse {
    String accessToken;
}