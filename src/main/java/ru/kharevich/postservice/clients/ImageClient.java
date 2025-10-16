package ru.kharevich.postservice.clients;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.config.FeignConfig;
import ru.kharevich.postservice.controller.exception.ImageServiceErrorDecoder;
import ru.kharevich.postservice.dto.request.ImageRequest;
import ru.kharevich.postservice.dto.response.ImageResponse;
import ru.kharevich.postservice.model.ImageType;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "image-client",
        configuration = {ImageServiceErrorDecoder.class, FeignConfig.class}
)
public interface ImageClient{


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImageResponse uploadImage(
            @RequestPart("imageType") String imageType,
            @RequestPart("parentEntityId") String parentEntityId,
            @RequestPart("file") List<MultipartFile> file);

    @GetMapping("/parent")
    ImageResponse getImageByParentId(@RequestParam UUID id);

    @DeleteMapping("/parent")
    void deleteImageByParentId(UUID id);

}
