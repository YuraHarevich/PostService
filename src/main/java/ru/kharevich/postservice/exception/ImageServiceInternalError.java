package ru.kharevich.postservice.exception;

public class ImageServiceInternalError extends RuntimeException {
    public ImageServiceInternalError(String message) {
        super(message);
    }
}
