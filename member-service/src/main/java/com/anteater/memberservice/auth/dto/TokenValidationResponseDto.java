package com.anteater.memberservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResponseDto {
    private boolean isValid;
}