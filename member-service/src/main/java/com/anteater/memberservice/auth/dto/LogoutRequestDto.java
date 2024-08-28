package com.anteater.memberservice.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LogoutRequestDto {
    @NotNull
    private String username;
}