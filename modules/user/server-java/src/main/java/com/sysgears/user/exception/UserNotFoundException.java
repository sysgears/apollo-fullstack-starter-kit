package com.sysgears.user.exception;

import com.sysgears.core.exception.FieldErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.Map;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends FieldErrorException {

    public UserNotFoundException(String message) {
        super(message, Collections.emptyMap());
    }

    public UserNotFoundException(String message, Map<String, String> errors) {
        super(message, errors);
    }
}
