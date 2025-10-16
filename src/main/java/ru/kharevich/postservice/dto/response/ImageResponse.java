package ru.kharevich.postservice.dto.response;

import ru.kharevich.postservice.dto.transferObject.FileTransferEntity;
import ru.kharevich.postservice.model.ImageType;

import java.util.List;

public record ImageResponse(

        ImageType imageType,

        List<FileTransferEntity> files

) {
}
