package com.mss.user_service.exceptions;

public class ProfileAlreadyCompletedException extends RuntimeException {
    public ProfileAlreadyCompletedException(String message) {
        super(message);
    }
}

