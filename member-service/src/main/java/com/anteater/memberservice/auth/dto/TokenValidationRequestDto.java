package com.anteater.memberservice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class TokenValidationRequestDto {
    @NotBlank
    private String token;
}
