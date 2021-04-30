package com.sysgears.post.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Integer id) {
        super(String.format("Comment with id %d not found", id));
    }
}
