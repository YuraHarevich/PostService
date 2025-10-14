package ru.kharevich.postservice.dto.response;

import ru.kharevich.postservice.model.ImageType;

import java.util.UUID;

public record ImageResponse(

        UUID id,

        String url,

        ImageType imageType,

        String name,

        byte[] file

) {
}
