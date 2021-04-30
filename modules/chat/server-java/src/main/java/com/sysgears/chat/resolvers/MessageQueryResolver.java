package com.sysgears.chat.resolvers;

import com.sysgears.chat.dto.MessagePayload;
import com.sysgears.chat.dto.Messages;
import com.sysgears.chat.service.MessageService;
import com.sysgears.core.pagination.OffsetPageRequest;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class MessageQueryResolver implements GraphQLQueryResolver {
    private final MessageService messageService;

    public CompletableFuture<Messages> messages(Optional<Integer> limit, Optional<Integer> after) {
        return CompletableFuture.supplyAsync(() -> {
            int offset = after.orElse(0);
            int size = limit.orElse(50);
            Pageable pageRequest = new OffsetPageRequest(offset, size);

            return messageService.findAll(pageRequest);
        });
    }

    public CompletableFuture<MessagePayload> message(Integer id) {
        return messageService.getById(id);
    }
}
