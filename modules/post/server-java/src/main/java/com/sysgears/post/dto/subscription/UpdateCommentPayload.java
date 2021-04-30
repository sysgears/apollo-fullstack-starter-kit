package com.sysgears.post.dto.subscription;

import com.sysgears.post.dto.CommentPayload;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class UpdateCommentPayload {
    private final Integer id;
    @NonNull
    private final Integer postId;
    @NonNull
    private final String mutation;
    private final CommentPayload node;

}
