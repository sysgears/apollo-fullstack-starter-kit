package com.sysgears.post.resolvers;

import com.sysgears.core.subscription.Publisher;
import com.sysgears.post.dto.PostPayload;
import com.sysgears.post.dto.input.AddPostInput;
import com.sysgears.post.dto.input.EditPostInput;
import com.sysgears.post.model.Post;
import com.sysgears.post.service.PostService;
import com.sysgears.post.subscription.Mutation;
import com.sysgears.post.subscription.PostUpdatedEvent;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class PostMutationResolver implements GraphQLMutationResolver {
    private final PostService postService;
    private final Publisher<PostUpdatedEvent> postPublisher;

    public CompletableFuture<PostPayload> addPost(AddPostInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Post post = new Post(input.getTitle(), input.getContent());
            Post created = postService.create(post);

            PostPayload postPayload = new PostPayload(created.getId(), created.getTitle(), created.getContent());
            postPublisher.publish(new PostUpdatedEvent(Mutation.CREATED, postPayload));

            return postPayload;
        });
    }

    public CompletableFuture<PostPayload> deletePost(Integer id) {
        return CompletableFuture.supplyAsync(() -> {
            Post deletedPost = postService.deleteById(id);

            PostPayload postPayload = new PostPayload(deletedPost.getId(), deletedPost.getTitle(), deletedPost.getContent());
            postPublisher.publish(new PostUpdatedEvent(Mutation.DELETED, postPayload));

            return postPayload;
        });
    }

    public CompletableFuture<PostPayload> editPost(EditPostInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Post post = postService.findById(input.getId());
            post.setTitle(input.getTitle());
            post.setContent(input.getContent());

            Post updated = postService.update(post);

            PostPayload postPayload = new PostPayload(updated.getId(), updated.getTitle(), updated.getContent());
            postPublisher.publish(new PostUpdatedEvent(Mutation.UPDATED, postPayload));

            return postPayload;
        });
    }
}
