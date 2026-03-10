package com.mss.user_service.exceptions;

import com.mss.user_service.payloads.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse> handleUserNotFoundException(UserNotFoundException ex) {
        BaseResponse response = new BaseResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProfileAlreadyCompletedException.class)
    public ResponseEntity<BaseResponse> handleProfileAlreadyCompletedException(ProfileAlreadyCompletedException ex) {
        BaseResponse response = new BaseResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.CONFLICT.value()),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidAddressIndexException.class)
    public ResponseEntity<BaseResponse> handleInvalidAddressIndexException(InvalidAddressIndexException ex) {
        BaseResponse response = new BaseResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxAddressLimitException.class)
    public ResponseEntity<BaseResponse> handleMaxAddressLimitException(MaxAddressLimitException ex) {
        BaseResponse response = new BaseResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        BaseResponse response = new BaseResponse(
                "Validation failed",
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse> handleAccessDeniedException(AccessDeniedException ex) {
        BaseResponse response = new BaseResponse(
                "Access denied",
                String.valueOf(HttpStatus.FORBIDDEN.value()),
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGlobalException(Exception ex) {
        BaseResponse response = new BaseResponse(
                "Internal server error: " + ex.getMessage(),
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

