package ru.kharevich.postservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record PostRequest(
        @NotNull(message = "text for post is required")
        String text,

        String author
) {
}
