package ru.kharevich.postservice.clients;

import jakarta.validation.constraints.Min;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.config.FeignConfig;
import ru.kharevich.postservice.controller.exception.ImageServiceErrorDecoder;
import ru.kharevich.postservice.dto.response.ImageResponse;
import ru.kharevich.postservice.dto.response.PageableResponse;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "image-client",
        configuration = {ImageServiceErrorDecoder.class, FeignConfig.class}
)
public interface ImageClient {


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImageResponse uploadImage(
            @RequestPart("imageType") String imageType,
            @RequestPart("parentEntityId") String parentEntityId,
            @RequestPart("file") List<MultipartFile> file);

    @GetMapping("/parent")
    ImageResponse getImageByParentId(@RequestParam UUID id);

    @GetMapping("/parent/many")
    PageableResponse<ImageResponse> getImagesByParentId(@RequestParam List<UUID> ids,
                                                        @RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                        @RequestParam(defaultValue = "10") int size);

    @DeleteMapping("/parent")
    void deleteImageByParentId(UUID id);

}
