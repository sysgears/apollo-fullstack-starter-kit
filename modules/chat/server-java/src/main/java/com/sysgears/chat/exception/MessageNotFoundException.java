package com.sysgears.chat.exception;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(Integer id) {
        super(String.format("Message with id %d not found", id));
    }
}
