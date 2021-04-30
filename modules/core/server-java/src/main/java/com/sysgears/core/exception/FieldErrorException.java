package com.sysgears.core.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FieldErrorException extends RuntimeException implements GraphQLError {
    private final Map<String, String> errors = new HashMap<>();

    public FieldErrorException(String message, Map<String, String> errors) {
        super(message);
        this.errors.putAll(errors);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Map.of("exception", Map.of("errors", errors));
    }
}
