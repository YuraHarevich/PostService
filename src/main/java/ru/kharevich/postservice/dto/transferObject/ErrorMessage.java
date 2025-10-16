package ru.kharevich.postservice.dto.transferObject;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
public class ErrorMessage {

    private String message;

    private LocalDateTime timestamp;

}