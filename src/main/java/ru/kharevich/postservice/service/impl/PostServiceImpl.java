package ru.kharevich.postservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.clients.ImageClient;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.ImageResponse;
import ru.kharevich.postservice.dto.response.PageableResponse;
import ru.kharevich.postservice.dto.response.PostResponse;
import ru.kharevich.postservice.exception.PostNotFoundException;
import ru.kharevich.postservice.model.ImageType;
import ru.kharevich.postservice.model.Post;
import ru.kharevich.postservice.repository.PostRepository;
import ru.kharevich.postservice.service.PostService;
import ru.kharevich.postservice.util.mapper.PageMapper;
import ru.kharevich.postservice.util.mapper.PostMapper;
import ru.kharevich.postservice.util.validation.PostValidationService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ImageClient imageClient;
    private final PostMapper postMapper;
    private final PostValidationService postValidationService;
    private final PageMapper pageMapper;

    public PostResponse create(PostRequest request, List<MultipartFile> files) {
        Post post = postMapper.toEntity(request);
        postRepository.saveAndFlush(post);
//        ImageRequest imageRequest = new ImageRequest(ImageType.POST_ATTACHMENT, post.getId(),files.getFirst(),"name");
//        imageClient.uploadImage(imageRequest);
        imageClient.uploadImage(ImageType.POST_ATTACHMENT.toString(), post.getId().toString(),files.getFirst());
        byte[] bytes;
        try {
            bytes = files.getFirst().getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return postMapper.toResponse(post, bytes);
    }

    public void delete(UUID id) {
        postValidationService.findByIdThrowsExceptionIfDoesntExist(id,
                new PostNotFoundException("post not found"));
        postRepository.deleteById(id);
        imageClient.deleteImageByParentId(id);
    }

    public PostResponse update(PostRequest request, UUID id) {
        Post post = postValidationService.findByIdThrowsExceptionIfDoesntExist(id,
                new PostNotFoundException("post not found"));
        postMapper.updateEntityByRequest(request, post);
        return postMapper.toResponse(post, null);
    }

    public PageableResponse<PostResponse> getFeed(int pageNumber, int size) {
        Page<Post> posts = postRepository.findAll(PageRequest.of(pageNumber, size));
        Page<PostResponse> postResponses = posts.map(post -> postMapper.toResponse(post, null));

        return pageMapper.toResponse(postResponses);
    }

    public PostResponse getById(UUID id) {
        Post post = postValidationService.findByIdThrowsExceptionIfDoesntExist(id,
                new PostNotFoundException("post not found"));
        ImageResponse imageResponse = imageClient.getImageByParentId(id);
        return postMapper.toResponse(post, imageResponse.file());
    }

    public PageableResponse<PostResponse> getPostsByAuthor(int pageNumber,
                                                           int size,
                                                           UUID author) {

        return pageMapper.toResponse(
                postRepository.findByAuthorId(author, PageRequest.of(pageNumber,size))
        );
    }

}
