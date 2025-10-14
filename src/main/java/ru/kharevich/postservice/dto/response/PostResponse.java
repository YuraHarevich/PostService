package ru.kharevich.postservice.dto.response;

import java.util.UUID;

public record PostResponse (

        UUID id,

        String text,

        UUID authorId,

        byte[] file
){
}
