package ru.kharevich.postservice.controller.impl;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.controller.api.PostController;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.PageableResponse;
import ru.kharevich.postservice.dto.response.PostResponse;
import ru.kharevich.postservice.service.PostService;
import ru.kharevich.postservice.util.annotations.NotEmptyFiles;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/posts")
@Validated
public class PostControllerImpl implements PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PostResponse create(@RequestPart("body") PostRequest request,
                               @RequestPart("files") @NotEmptyFiles List<MultipartFile> files) {
        PostResponse response = postService.create(request, files);
        return response;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        postService.delete(id);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PostResponse update(PostRequest request, @PathVariable UUID id) {
        return postService.update(request, id);
    }

    @GetMapping("feed")
    @ResponseStatus(HttpStatus.OK)
    public PageableResponse<PostResponse> getFeed(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                  @RequestParam(defaultValue = "10") int size) {
        return postService.getFeed(page_number, size);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponse getById(@PathVariable UUID id) {
        return postService.getById(id);
    }

    @GetMapping("author/{author}")
    @ResponseStatus(HttpStatus.OK)
    public PageableResponse<PostResponse> getPostsByAuthor(int page_number, int size, @PathVariable String author) {
        return postService.getPostsByAuthor(page_number, size, author);
    }

}
