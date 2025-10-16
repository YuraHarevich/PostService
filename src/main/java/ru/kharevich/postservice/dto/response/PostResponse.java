package ru.kharevich.postservice.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponse (

        UUID id,

        String text,

        String author,

        Integer numberOfLikes,

        Integer numberOfComments,

        LocalDateTime createdAt,

        List<byte[]> files

){
}
