package com.sysgears.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Posts {
    private Long totalCount;
    private List<PostEdges> edges;
    private PostPageInfo pageInfo;
}
