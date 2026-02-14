package com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;

public class DecryptException extends ApiException {
    public DecryptException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

