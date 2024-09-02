package com.anteater.memberservice.common.exception;

import io.jsonwebtoken.JwtException;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}