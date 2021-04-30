package com.sysgears.upload.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(Integer id) {
        super(String.format("File with id %d not found", id));
    }
}
