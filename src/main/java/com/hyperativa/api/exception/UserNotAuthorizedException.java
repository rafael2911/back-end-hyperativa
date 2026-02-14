package com.hyperativa.api.exception;

import org.springframework.http.HttpStatus;

public class UserNotAuthorizedException extends ApiException {
    public UserNotAuthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

