package com.mss.user_service.exceptions;

public class MaxAddressLimitException extends RuntimeException {
    public MaxAddressLimitException(String message) {
        super(message);
    }
}

