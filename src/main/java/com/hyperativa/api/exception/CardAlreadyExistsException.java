package com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;

public class CardAlreadyExistsException extends ApiException {
    public CardAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

