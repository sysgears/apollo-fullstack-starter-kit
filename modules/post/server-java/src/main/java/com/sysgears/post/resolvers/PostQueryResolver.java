package com.sysgears.post.resolvers;

import com.sysgears.post.dto.PostPayload;
import com.sysgears.post.dto.Posts;
import com.sysgears.core.pagination.OffsetPageRequest;
import com.sysgears.post.service.PostService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class PostQueryResolver implements GraphQLQueryResolver {
    private final PostService postService;

    public CompletableFuture<Posts> posts(Optional<Integer> limit, Optional<Integer> after) {
        return CompletableFuture.supplyAsync(() -> {
            int offset = after.orElse(0);
            int size = limit.orElse(10);
            Pageable pageRequest = new OffsetPageRequest(offset, size);
            return postService.findAll(pageRequest);
        });
    }

    public CompletableFuture<PostPayload> post(Integer id) {
        return postService.getById(id);
    }
}
