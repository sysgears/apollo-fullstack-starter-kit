package com.sysgears.core.exception;

import graphql.GraphQLError;
import graphql.kickstart.execution.error.GenericGraphQLError;

import java.util.Map;
import java.util.Objects;

public class CustomGraphQLException extends GenericGraphQLError {

    private final Throwable throwable;

    public CustomGraphQLException(Throwable throwable) {
        this(throwable, throwable.getMessage());
    }

    public CustomGraphQLException(Throwable throwable, String message) {
        super(message);

        this.throwable = throwable;
    }

    public String getType() {
        return throwable.getClass().getSimpleName();
    }

    @Override
    public Map<String, Object> getExtensions() {
        if (throwable instanceof GraphQLError) {
            return ((GraphQLError) throwable).getExtensions();
        } else {
            return null;
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomGraphQLException)) {
            return false;
        }
        CustomGraphQLException that = (CustomGraphQLException) o;
        return Objects.equals(throwable, that.throwable) && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(throwable, getMessage());
    }
}
