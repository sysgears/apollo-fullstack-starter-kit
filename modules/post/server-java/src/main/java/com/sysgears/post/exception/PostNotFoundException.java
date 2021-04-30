package com.sysgears.post.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Integer id) {
        super(String.format("Post with id %d not found", id));
    }
}
