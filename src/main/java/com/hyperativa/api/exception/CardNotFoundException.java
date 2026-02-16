package com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;

public class CardNotFoundException extends ApiException {
    public CardNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

