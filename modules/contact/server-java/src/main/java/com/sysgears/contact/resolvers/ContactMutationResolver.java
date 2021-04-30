package com.sysgears.contact.resolvers;

import com.sysgears.contact.dto.ContactInput;
import com.sysgears.mailer.service.EmailService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ContactMutationResolver implements GraphQLMutationResolver {
    private final EmailService emailService;

    public CompletableFuture<String> contact(ContactInput input) {
        return CompletableFuture.supplyAsync(() -> {
            emailService.sendContactUsEmail(input.getName(), input.getEmail(), input.getContent());
            return null;
        });
    }
}
