package com.sysgears.post.resolvers;

import com.sysgears.core.subscription.Subscriber;
import com.sysgears.post.dto.subscription.UpdateCommentPayload;
import com.sysgears.post.subscription.CommentUpdatedEvent;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CommentSubscriptionResolver implements GraphQLSubscriptionResolver {
    private final Subscriber<CommentUpdatedEvent> commentSubscriber;

    public CompletableFuture<Publisher<UpdateCommentPayload>> commentUpdated(Integer postId) {
        return CompletableFuture.supplyAsync(() -> commentSubscriber.subscribe(e -> e.getPostId().equals(postId))
                .map(event ->
                        new UpdateCommentPayload(
                                event.getComment().getId(),
                                event.getPostId(),
                                event.getMutation().name(),
                                event.getComment()
                        )
                )
        );
    }
}
