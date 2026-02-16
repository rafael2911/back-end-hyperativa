package com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;

public class EncryptException extends ApiException {
    public EncryptException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

