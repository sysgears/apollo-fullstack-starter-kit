package com.sysgears.post.subscription;

import com.sysgears.post.dto.CommentPayload;
import lombok.Data;

@Data
public class CommentUpdatedEvent {
    private final Mutation mutation;
    private final Integer postId;
    private final CommentPayload comment;
}
