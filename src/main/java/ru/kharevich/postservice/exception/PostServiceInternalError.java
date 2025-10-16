package ru.kharevich.postservice.exception;

public class PostServiceInternalError extends RuntimeException {
    public PostServiceInternalError(String message) {
        super(message);
    }
}
