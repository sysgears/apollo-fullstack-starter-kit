package com.sysgears.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentPayload {
    @NonNull
    private Integer id;
    @NonNull
    private String content;
}
