package ru.kharevich.postservice.dto.transferObject;

public record FileTransferEntity(
        byte[] file,
        String name
) {
}
