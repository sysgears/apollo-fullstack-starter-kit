package com.sysgears.post.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCommentInput {
    @NonNull
    private Integer id;
    @NonNull
    private Integer postId;
}
