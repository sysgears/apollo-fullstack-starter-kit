package com.sysgears.chat.resolvers;

import com.sysgears.chat.dto.subscription.UpdateMessagesPayload;
import com.sysgears.chat.subscription.MessageUpdatedEvent;
import com.sysgears.core.subscription.Subscriber;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class MessageSubscriptionResolver implements GraphQLSubscriptionResolver {
	private final Subscriber<MessageUpdatedEvent> messageSubscriber;

	public CompletableFuture<Publisher<UpdateMessagesPayload>> messagesUpdated(Integer endCursor) {
		return CompletableFuture.supplyAsync(() -> messageSubscriber.subscribe(e -> e.getMessage().getId() <= endCursor)
				.map(event -> new UpdateMessagesPayload(event.getMutation().name(), event.getMessage().getId(), event.getMessage())));
	}
}
