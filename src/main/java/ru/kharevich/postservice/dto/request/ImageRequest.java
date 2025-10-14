package ru.kharevich.postservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.model.ImageType;

import java.util.UUID;

public record ImageRequest(

        @NotNull(message = "Image type is required")
        ImageType imageType,

        @NotNull(message = "Parent entity ID is required")
        UUID parentEntityId,

        @NotNull(message = "File is required")
        MultipartFile file
) {

}
