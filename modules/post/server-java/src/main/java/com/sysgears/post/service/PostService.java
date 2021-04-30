package com.sysgears.post.service;

import com.sysgears.post.dto.*;
import com.sysgears.post.exception.CommentNotFoundException;
import com.sysgears.post.exception.PostNotFoundException;
import com.sysgears.post.model.Comment;
import com.sysgears.post.model.Post;
import com.sysgears.post.repository.CommentRepository;
import com.sysgears.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository repository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Posts findAll(Pageable pageable) {
        Page<Post> posts = repository.findAll(pageable);

        List<PostEdges> postEdgesList = new ArrayList<>();

        for (int i = 0; i < posts.getContent().size(); i++) {
            Post post = posts.getContent().get(i);
            postEdgesList.add(
                    PostEdges.builder()
                            .cursor((int) (pageable.getOffset() + i))
                            .node(new PostPayload(post.getId(), post.getTitle(), post.getContent()))
                            .build()
            );
        }

        return Posts.builder()
                .edges(postEdgesList)
                .pageInfo(
                        PostPageInfo.builder()
                                .endCursor((int) (pageable.getOffset() + posts.getSize() - 1))
                                .hasNextPage(!posts.isLast())
                                .build()
                )
                .totalCount(posts.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public CompletableFuture<PostPayload> getById(Integer id) {
        return repository.getById(id).thenApply(post -> {
            if (post == null) throw new PostNotFoundException(id);

            PostPayload postPayload = new PostPayload(id, post.getTitle(), post.getContent());
            postPayload.addComments(convertComments(post.getComments()));
            return postPayload;
        });
    }

    @Transactional(readOnly = true)
    public Post findById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    @Transactional
    public Post create(Post post) {
        return repository.save(post);
    }

    @Transactional
    public Post deleteById(Integer id) {
        Post post = findById(id);

        repository.delete(post);
        return post;
    }

    @Transactional
    public Post update(Post post) {
        return repository.save(post);
    }

    @Transactional
    public Comment addComment(Integer postId, String commentContent) {
        Post post = findById(postId);

        Comment comment = commentRepository.save(new Comment(commentContent));
        post.addComment(comment);

        update(post);

        return comment;
    }

    @Transactional
    public Comment deleteComment(Integer postId, Integer commentId) {
        Post post = findById(postId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        post.removeComment(comment);

        update(post);

        return comment;
    }

    @Transactional
    public Comment editComment(Integer postId, Integer commentId, String content) {
        Post post = findById(postId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        comment.setContent(content);

        update(post);

        return comment;
    }

    private Set<CommentPayload> convertComments(Collection<Comment> comments) {
        return comments.stream()
                .map(comment -> new CommentPayload(comment.getId(), comment.getContent()))
                .collect(Collectors.toSet());
    }
}
