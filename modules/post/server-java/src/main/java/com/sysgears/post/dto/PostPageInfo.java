package com.sysgears.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPageInfo {
    private Integer endCursor;
    private Boolean hasNextPage;
}
