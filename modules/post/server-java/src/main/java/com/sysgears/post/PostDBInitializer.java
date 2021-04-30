package com.sysgears.post;

import com.sysgears.post.model.Comment;
import com.sysgears.post.model.Post;
import com.sysgears.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostDBInitializer {
    private final PostRepository repository;

    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent event) {
        long count = repository.count();
        if (count == 0) {
            List<Post> posts = IntStream.rangeClosed(1, 20).mapToObj(i -> {
                Post post = new Post();
                post.setTitle("Post title " + i);
                post.setContent("Some content " + i);
                Comment comment = new Comment();
                comment.setContent("123");
                post.setComments(Set.of(comment));
                return post;
            }).collect(Collectors.toList());
            repository.saveAll(posts);
        }
    }
}
