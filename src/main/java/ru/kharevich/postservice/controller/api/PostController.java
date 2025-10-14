package ru.kharevich.postservice.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.PageableResponse;
import ru.kharevich.postservice.dto.response.PostResponse;

import java.util.List;
import java.util.UUID;

public interface PostController {

    PostResponse create(@RequestPart("body") PostRequest request,
                               @RequestPart("files") List<MultipartFile> files);

    void delete(@Valid UUID id);

    PostResponse update(@Valid PostRequest request,@PathVariable UUID id);

    PageableResponse<PostResponse> getFeed(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                           @RequestParam(defaultValue = "20") int size);

    PostResponse getById(@Valid UUID id);

    PageableResponse<PostResponse> getPostsByAuthor(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                    @RequestParam(defaultValue = "20") int size, @PathVariable UUID author);

}
