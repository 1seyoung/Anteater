package com.anteater.memberservice.auth.dto.request;

import lombok.Value;

@Value
public class TokenRequest {
    Long userId;
    String username;
}