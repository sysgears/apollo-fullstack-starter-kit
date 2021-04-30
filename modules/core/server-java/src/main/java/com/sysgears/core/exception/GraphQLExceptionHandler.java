package com.sysgears.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class GraphQLExceptionHandler {

    @ExceptionHandler(CompletionException.class)
    public CustomGraphQLException handle(CompletionException exception) {
        if (exception.getCause() != null) {
            return new CustomGraphQLException(exception.getCause());
        } else {
            return new CustomGraphQLException(exception);
        }
    }
}
