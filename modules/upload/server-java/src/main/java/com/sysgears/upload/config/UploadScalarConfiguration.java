package com.sysgears.upload.config;

import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.Part;

@Configuration
public class UploadScalarConfiguration {

    @Bean
    public GraphQLScalarType uploadScalar() {
        return GraphQLScalarType.newScalar()
                .name("FileUpload")
                .description("A file part in a multipart request")
                .coercing(new Coercing<Part, Void>() {
                    @Override
                    public Void serialize(Object dataFetcherResult) {
                        throw new CoercingSerializeException("Upload is an input-only type");
                    }

                    @Override
                    public Part parseValue(Object input) {
                        if (input instanceof Part) {
                            return (Part) input;
                        } else if (null == input) {
                            return null;
                        } else {
                            throw new CoercingParseValueException("Expected type " +
                                    Part.class.getName() +
                                    " but was " +
                                    input.getClass().getName());
                        }
                    }

                    @Override
                    public Part parseLiteral(Object input) {
                        throw new CoercingParseLiteralException(
                                "Must use variables to specify Upload values");
                    }
                })
                .build();
    }
}
