package com.sysgears.post.resolvers;

import com.sysgears.core.subscription.Publisher;
import com.sysgears.post.dto.CommentPayload;
import com.sysgears.post.dto.input.AddCommentInput;
import com.sysgears.post.dto.input.DeleteCommentInput;
import com.sysgears.post.dto.input.EditCommentInput;
import com.sysgears.post.model.Comment;
import com.sysgears.post.service.PostService;
import com.sysgears.post.subscription.CommentUpdatedEvent;
import com.sysgears.post.subscription.Mutation;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CommentMutationResolver implements GraphQLMutationResolver {
    private final PostService postService;
    private final Publisher<CommentUpdatedEvent> commentPublisher;

    public CompletableFuture<CommentPayload> addComment(AddCommentInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Comment comment = postService.addComment(input.getPostId(), input.getContent());

            CommentPayload commentPayload = new CommentPayload(comment.getId(), comment.getContent());
            commentPublisher.publish(new CommentUpdatedEvent(Mutation.CREATED, input.getPostId(),  commentPayload));

            return commentPayload;
        });
    }

    public CompletableFuture<CommentPayload> deleteComment(DeleteCommentInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Comment deletedComment = postService.deleteComment(input.getPostId(), input.getId());

            CommentPayload commentPayload = new CommentPayload(deletedComment.getId(), deletedComment.getContent());
            commentPublisher.publish(new CommentUpdatedEvent(Mutation.DELETED, input.getPostId(), commentPayload));

            return commentPayload;
        });
    }

    public CompletableFuture<CommentPayload> editComment(EditCommentInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Comment editedComment = postService.editComment(input.getPostId(), input.getId(), input.getContent());

            CommentPayload commentPayload = new CommentPayload(editedComment.getId(), editedComment.getContent());
            commentPublisher.publish(new CommentUpdatedEvent(Mutation.UPDATED, input.getPostId(), commentPayload));

            return commentPayload;
        });
    }
}
