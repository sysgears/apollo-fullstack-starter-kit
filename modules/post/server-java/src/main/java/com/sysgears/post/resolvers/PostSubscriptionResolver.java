package com.sysgears.post.resolvers;

import com.sysgears.core.subscription.Subscriber;
import com.sysgears.post.dto.subscription.UpdatePostPayload;
import com.sysgears.post.subscription.Mutation;
import com.sysgears.post.subscription.PostUpdatedEvent;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class PostSubscriptionResolver implements GraphQLSubscriptionResolver {
    private final Subscriber<PostUpdatedEvent> postSubscriber;

    public CompletableFuture<Publisher<UpdatePostPayload>> postUpdated(Integer id) {
        return CompletableFuture.supplyAsync(() -> postSubscriber.subscribe(e ->
                e.getPost().getId().equals(id) && (e.getMutation() == Mutation.UPDATED || e.getMutation() == Mutation.DELETED))
                .map(event -> new UpdatePostPayload(event.getPost().getId(), event.getMutation().name(), event.getPost()))
        );
    }

    public CompletableFuture<Publisher<UpdatePostPayload>> postsUpdated(Integer endCursor) {
        return CompletableFuture.supplyAsync(() -> postSubscriber.subscribe(e -> e.getPost().getId() <= endCursor)
                        .map(event -> new UpdatePostPayload(event.getPost().getId(), event.getMutation().name(), event.getPost()))
        );
    }
}
