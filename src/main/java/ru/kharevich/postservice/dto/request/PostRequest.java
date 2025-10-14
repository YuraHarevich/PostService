package ru.kharevich.postservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PostRequest(
        @NotNull(message = "text for post is required")
        String text,

        @Valid
        UUID authorId
) {
}
