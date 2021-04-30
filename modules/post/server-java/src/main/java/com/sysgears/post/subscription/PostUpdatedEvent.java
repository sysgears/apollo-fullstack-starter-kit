package com.sysgears.post.subscription;

import com.sysgears.post.dto.PostPayload;
import lombok.Data;

@Data
public class PostUpdatedEvent {
    private final Mutation mutation;
    private final PostPayload post;
}
