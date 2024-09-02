package com.anteater.activityservice.common.exception;

// Post 수정 시간 초과 예외
public class PostModificationTimeExceededException extends RuntimeException {
    public PostModificationTimeExceededException(String message) {
        super(message);
    }
}