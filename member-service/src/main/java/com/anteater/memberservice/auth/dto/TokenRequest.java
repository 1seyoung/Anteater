package com.anteater.memberservice.auth.dto;

public record TokenRequest(Long userId, String username) {
}