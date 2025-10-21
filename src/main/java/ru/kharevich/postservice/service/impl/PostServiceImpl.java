package ru.kharevich.postservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.clients.ImageClient;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.ActivityResponse;
import ru.kharevich.postservice.dto.response.ImageResponse;
import ru.kharevich.postservice.dto.response.PageableResponse;
import ru.kharevich.postservice.dto.response.PostResponse;
import ru.kharevich.postservice.dto.transferObject.FileTransferEntity;
import ru.kharevich.postservice.exception.PostNotFoundException;
import ru.kharevich.postservice.exception.PostServiceInternalError;
import ru.kharevich.postservice.model.ImageType;
import ru.kharevich.postservice.model.Post;
import ru.kharevich.postservice.repository.PostRepository;
import ru.kharevich.postservice.service.PostService;
import ru.kharevich.postservice.util.mapper.PageMapper;
import ru.kharevich.postservice.util.mapper.PostMapper;
import ru.kharevich.postservice.util.validation.PostValidationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.kharevich.postservice.util.constants.PostServiceResponseMessages.IMAGE_PARSE_ERROR_MESSAGE;
import static ru.kharevich.postservice.util.constants.PostServiceResponseMessages.POST_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ImageClient imageClient;
    private final PostMapper postMapper;
    private final PostValidationService postValidationService;
    private final PageMapper pageMapper;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PostResponse create(PostRequest request, List<MultipartFile> files) {

        Post post = postMapper.toEntity(request);
        postRepository.saveAndFlush(post);
        imageClient.uploadImage(ImageType.POST_ATTACHMENT.toString(), post.getId().toString(), files);
        List<byte[]> bytes = new ArrayList<>();
        files.forEach(imageFile -> {
            try {
                bytes.add(imageFile.getBytes());
            } catch (IOException e) {
                throw new PostServiceInternalError(IMAGE_PARSE_ERROR_MESSAGE);
            }
        });
        PostResponse response = postMapper.toResponse(post, bytes);
        return response;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(UUID id) {
        postValidationService.findByIdThrowsExceptionIfDoesntExist(id,
                new PostNotFoundException(POST_NOT_FOUND_MESSAGE));
        postRepository.deleteById(id);
        imageClient.deleteImageByParentId(id);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PostResponse update(PostRequest request, UUID id) {
        Post post = postValidationService.findByIdThrowsExceptionIfDoesntExist(id,
                new PostNotFoundException(POST_NOT_FOUND_MESSAGE));
        postMapper.updateEntityByRequest(request, post);
        return postMapper.toResponse(post, null);
    }

    public PageableResponse<PostResponse> getFeed(int pageNumber, int size) {
        Page<Post> posts = postRepository.findAll(PageRequest.of(pageNumber, size));
        return fillPostsWithImages(posts, pageNumber, size);
    }

    public PostResponse getById(UUID id) {
        Post post = postValidationService.findByIdThrowsExceptionIfDoesntExist(id,
                new PostNotFoundException(POST_NOT_FOUND_MESSAGE));
        ImageResponse imageResponse = imageClient.getImageByParentId(id);
        return postMapper.toResponse(post, imageResponse.files().stream().map(FileTransferEntity::file).toList());
    }

    public PageableResponse<PostResponse> getPostsByAuthor(int pageNumber,
                                                           int size,
                                                           String author) {
        Page<Post> posts = postRepository.findByAuthor(author, PageRequest.of(pageNumber, size));
        return fillPostsWithImages(posts, pageNumber, size);
    }

    public void updateActivity(ActivityResponse activityResponse) {
        Post post = postRepository.findById(activityResponse.postId()).orElseThrow(() -> new PostNotFoundException("Post not found"));
        post.setNumberOfComments(activityResponse.numberOfComments());
        post.setNumberOfLikes(activityResponse.numberOfLikes());
        postRepository.save(post);
    }

    private PageableResponse<PostResponse> fillPostsWithImages(Page<Post> posts,
                                                               int pageNumber,
                                                               int size) {
        List<UUID> ids = posts.map(Post::getId).stream().toList();
        PageableResponse<ImageResponse> images = imageClient.getImagesByParentId(ids, pageNumber, size);
        Page<PostResponse> postResponses = posts.map(post -> {
            List<byte[]> files = images.content()
                    .stream()
                    .filter(response -> response.parentId().equals(post.getId()))
                    .findFirst()
                    .get().files()
                    .stream().map(FileTransferEntity::file).toList();
            return postMapper.toResponse(post, files);
        });
        return pageMapper.toResponse(postResponses);
    }

}
