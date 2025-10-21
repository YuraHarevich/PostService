package ru.kharevich.postservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.ActivityResponse;
import ru.kharevich.postservice.dto.response.PageableResponse;
import ru.kharevich.postservice.dto.response.PostResponse;

import java.util.List;
import java.util.UUID;

public interface PostService {

    PostResponse create(PostRequest request, List<MultipartFile> files);

    void delete(UUID id);

    PostResponse update(PostRequest request, UUID id);

    PageableResponse<PostResponse> getFeed(int pageNumber, int size);

    PostResponse getById(UUID id);

    PageableResponse<PostResponse> getPostsByAuthor(int pageNumber, int size, String author);

    void updateActivity(ActivityResponse activityResponse);
}
