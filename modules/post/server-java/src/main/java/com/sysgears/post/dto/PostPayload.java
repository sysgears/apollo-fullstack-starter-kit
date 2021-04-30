package com.sysgears.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPayload {
    @NonNull
    private Integer id;
    @NonNull
    private String title;
    @NonNull
    private String content;

    private final List<CommentPayload> comments = new ArrayList<>();

    public void addComments(Collection<CommentPayload> comments) {
        this.comments.addAll(comments);
    }
}
