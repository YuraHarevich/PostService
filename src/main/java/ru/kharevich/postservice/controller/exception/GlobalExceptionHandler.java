package ru.kharevich.postservice.controller.exception;


import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kharevich.postservice.dto.transferObject.ErrorMessage;
import ru.kharevich.postservice.exception.ImageServiceInternalError;
import ru.kharevich.postservice.exception.PostNotFoundException;
import ru.kharevich.postservice.exception.PostServiceInternalError;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            PostNotFoundException.class
    })
    public ResponseEntity<ErrorMessage> handleNotFound(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorMessage.builder()
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler({
            ImageServiceInternalError.class,
            PostServiceInternalError.class
    })
    public ResponseEntity<ErrorMessage> handleServiceUnavailable(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorMessage.builder()
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }


    @ExceptionHandler({
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorMessage> handleBadRequest(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.builder()
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorMessage> handleValidationExceptions(Exception ex) {
        String errorMessage = ex instanceof MethodArgumentNotValidException
                ? ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().getFirst().getDefaultMessage()
                : ex.getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.builder()
                        .message(errorMessage)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler({
            Exception.class
    })
    public ResponseEntity<ErrorMessage> handleUnhandled(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorMessage.builder()
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

}


