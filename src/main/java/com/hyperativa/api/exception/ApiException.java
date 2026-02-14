package com.hyperativa.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus statusCode;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.statusCode = status;
    }
}
