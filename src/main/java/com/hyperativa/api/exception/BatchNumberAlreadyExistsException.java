package com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;

public class BatchNumberAlreadyExistsException extends ApiException {
    public BatchNumberAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

