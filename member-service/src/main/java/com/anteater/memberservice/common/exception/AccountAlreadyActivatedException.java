package com.anteater.memberservice.common.exception;

public class AccountAlreadyActivatedException extends RuntimeException {
    public AccountAlreadyActivatedException(String message) {
        super(message);
    }
}