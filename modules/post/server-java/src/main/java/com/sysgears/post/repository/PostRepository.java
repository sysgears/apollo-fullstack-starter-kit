package com.sysgears.post.repository;

import com.sysgears.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Async
    CompletableFuture<Post> getById(Integer id);
}
