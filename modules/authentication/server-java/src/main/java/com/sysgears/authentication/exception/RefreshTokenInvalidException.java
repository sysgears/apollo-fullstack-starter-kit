package com.sysgears.authentication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class RefreshTokenInvalidException extends RuntimeException {

    public RefreshTokenInvalidException() {
        super("Refresh token is invalid.");
    }
}
