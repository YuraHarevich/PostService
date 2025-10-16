package ru.kharevich.postservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.ImageResponse;
import ru.kharevich.postservice.dto.response.PageableResponse;
import ru.kharevich.postservice.dto.response.PostResponse;
import ru.kharevich.postservice.dto.transferObject.FileTransferEntity;
import ru.kharevich.postservice.exception.PostNotFoundException;
import ru.kharevich.postservice.exception.PostServiceInternalError;
import ru.kharevich.postservice.model.ImageType;
import ru.kharevich.postservice.model.Post;
import ru.kharevich.postservice.repository.PostRepository;
import ru.kharevich.postservice.util.mapper.PageMapper;
import ru.kharevich.postservice.util.mapper.PostMapper;
import ru.kharevich.postservice.util.validation.PostValidationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ru.kharevich.postservice.clients.ImageClient imageClient;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostValidationService postValidationService;

    @Mock
    private PageMapper pageMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void create_ShouldReturnPostResponse_WhenValidRequest() throws IOException {
        PostRequest request = new PostRequest("text", "author", UUID.randomUUID());
        List<MultipartFile> files = List.of(multipartFile);
        Post post = createTestPost();
        byte[] fileBytes = "file content".getBytes();
        PostResponse expectedResponse = createTestPostResponse();

        when(postMapper.toEntity(request)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(postMapper.toResponse(post, List.of(fileBytes))).thenReturn(expectedResponse);

        PostResponse result = postService.create(request, files);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(postMapper).toEntity(request);
        verify(postRepository).saveAndFlush(post);
        verify(imageClient).uploadImage(ImageType.POST_ATTACHMENT.toString(), post.getId().toString(), files);
        verify(postMapper).toResponse(post, List.of(fileBytes));
    }

    @Test
    void create_ShouldThrowPostServiceInternalError_WhenFileParsingFails() throws IOException {
        PostRequest request = new PostRequest("text", "author", UUID.randomUUID());
        List<MultipartFile> files = List.of(multipartFile);
        Post post = createTestPost();

        when(postMapper.toEntity(request)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(multipartFile.getBytes()).thenThrow(new IOException());

        assertThrows(PostServiceInternalError.class, () -> postService.create(request, files));

        verify(postMapper).toEntity(request);
        verify(postRepository).saveAndFlush(post);
        verify(imageClient).uploadImage(ImageType.POST_ATTACHMENT.toString(), post.getId().toString(), files);
    }

    @Test
    void create_ShouldHandleMultipleFiles() throws IOException {
        PostRequest request = new PostRequest("text", "author", UUID.randomUUID());
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(multipartFile, file2);
        Post post = createTestPost();
        byte[] fileBytes1 = "file1 content".getBytes();
        byte[] fileBytes2 = "file2 content".getBytes();
        PostResponse expectedResponse = createTestPostResponse();

        when(postMapper.toEntity(request)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(multipartFile.getBytes()).thenReturn(fileBytes1);
        when(file2.getBytes()).thenReturn(fileBytes2);
        when(postMapper.toResponse(post, List.of(fileBytes1, fileBytes2))).thenReturn(expectedResponse);

        PostResponse result = postService.create(request, files);

        assertNotNull(result);
        verify(postMapper).toResponse(post, List.of(fileBytes1, fileBytes2));
    }

    @Test
    void create_ShouldHandleEmptyFilesList() {
        PostRequest request = new PostRequest("text", "author", UUID.randomUUID());
        List<MultipartFile> files = new ArrayList<>();
        Post post = createTestPost();
        PostResponse expectedResponse = createTestPostResponse();

        when(postMapper.toEntity(request)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(postMapper.toResponse(post, new ArrayList<>())).thenReturn(expectedResponse);

        PostResponse result = postService.create(request, files);

        assertNotNull(result);
        verify(postMapper).toResponse(post, new ArrayList<>());
        verify(imageClient).uploadImage(ImageType.POST_ATTACHMENT.toString(), post.getId().toString(), files);
    }

    @Test
    void delete_ShouldDeletePost_WhenPostExists() {
        UUID postId = UUID.randomUUID();
        Post post = createTestPost();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenReturn(post);
        doNothing().when(postRepository).deleteById(postId);
        doNothing().when(imageClient).deleteImageByParentId(postId);

        postService.delete(postId);

        verify(postValidationService).findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class));
        verify(postRepository).deleteById(postId);
        verify(imageClient).deleteImageByParentId(postId);
    }

    @Test
    void delete_ShouldThrowException_WhenPostNotFound() {
        UUID postId = UUID.randomUUID();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenThrow(new PostNotFoundException("Post not found"));

        assertThrows(PostNotFoundException.class, () -> postService.delete(postId));

        verify(postValidationService).findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class));
        verify(postRepository, never()).deleteById(any());
        verify(imageClient, never()).deleteImageByParentId(any());
    }

    @Test
    void update_ShouldReturnUpdatedPostResponse_WhenPostExists() {
        UUID postId = UUID.randomUUID();
        PostRequest request = new PostRequest("updated text", "updated author", UUID.randomUUID());
        Post post = createTestPost();
        PostResponse expectedResponse = createTestPostResponse();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenReturn(post);
        doNothing().when(postMapper).updateEntityByRequest(request, post);
        when(postMapper.toResponse(post, null)).thenReturn(expectedResponse);

        PostResponse result = postService.update(request, postId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(postValidationService).findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class));
        verify(postMapper).updateEntityByRequest(request, post);
        verify(postMapper).toResponse(post, null);
    }

    @Test
    void update_ShouldThrowException_WhenPostNotFound() {
        UUID postId = UUID.randomUUID();
        PostRequest request = new PostRequest("text", "author", UUID.randomUUID());

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenThrow(new PostNotFoundException("Post not found"));

        assertThrows(PostNotFoundException.class, () -> postService.update(request, postId));

        verify(postValidationService).findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class));
        verify(postMapper, never()).updateEntityByRequest(any(), any());
        verify(postMapper, never()).toResponse(any(), any());
    }

    @Test
    void getById_ShouldHandleEmptyImageResponse() {
        UUID postId = UUID.randomUUID();
        Post post = createTestPost();
        ImageResponse imageResponse = new ImageResponse(ImageType.POST_ATTACHMENT, new ArrayList<>(), postId);
        PostResponse expectedResponse = createTestPostResponse();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenReturn(post);
        when(imageClient.getImageByParentId(postId)).thenReturn(imageResponse);
        when(postMapper.toResponse(post, new ArrayList<>())).thenReturn(expectedResponse);

        PostResponse result = postService.getById(postId);

        assertNotNull(result);
        verify(postMapper).toResponse(post, new ArrayList<>());
    }

    @Test
    void getById_ShouldThrowException_WhenPostNotFound() {
        UUID postId = UUID.randomUUID();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenThrow(new PostNotFoundException("Post not found"));

        assertThrows(PostNotFoundException.class, () -> postService.getById(postId));

        verify(postValidationService).findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class));
        verify(imageClient, never()).getImageByParentId(any());
        verify(postMapper, never()).toResponse(any(), any());
    }


    @Test
    void getFeed_ShouldHandleEmptyPostPage() {
        int pageNumber = 0;
        int size = 10;
        Page<Post> emptyPostPage = Page.empty();
        PageableResponse<ImageResponse> emptyImageResponse = new PageableResponse<>(0L, 0, 0, 10, new ArrayList<>());
        PageableResponse<PostResponse> expectedResponse = new PageableResponse<>(0L, 0, 0, 10, new ArrayList<>());

        when(postRepository.findAll(PageRequest.of(pageNumber, size))).thenReturn(emptyPostPage);
        when(imageClient.getImagesByParentId(anyList(), eq(pageNumber), eq(size))).thenReturn(emptyImageResponse);
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getFeed(pageNumber, size);

        assertNotNull(result);
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.content().size());
    }


    @Test
    void getPostsByAuthor_ShouldHandleEmptyAuthorPosts() {
        int pageNumber = 0;
        int size = 10;
        String author = "nonExistentAuthor";
        Page<Post> emptyPostPage = Page.empty();
        PageableResponse<ImageResponse> emptyImageResponse = new PageableResponse<>(0L, 0, 0, 10, new ArrayList<>());
        PageableResponse<PostResponse> expectedResponse = new PageableResponse<>(0L, 0, 0, 10, new ArrayList<>());

        when(postRepository.findByAuthor(author, PageRequest.of(pageNumber, size))).thenReturn(emptyPostPage);
        when(imageClient.getImagesByParentId(anyList(), eq(pageNumber), eq(size))).thenReturn(emptyImageResponse);
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getPostsByAuthor(pageNumber, size, author);

        assertNotNull(result);
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.content().size());
    }

    @Test
    void update_ShouldHandleNullFieldsInRequest() {
        UUID postId = UUID.randomUUID();
        PostRequest request = new PostRequest(null, null, null);
        Post post = createTestPost();
        PostResponse expectedResponse = createTestPostResponse();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenReturn(post);
        doNothing().when(postMapper).updateEntityByRequest(request, post);
        when(postMapper.toResponse(post, null)).thenReturn(expectedResponse);

        PostResponse result = postService.update(request, postId);

        assertNotNull(result);
        verify(postMapper).updateEntityByRequest(request, post);
    }

    @Test
    void getPostsByAuthor_ShouldHandleEmptyAuthorName() {
        int pageNumber = 0;
        int size = 10;
        String author = "";
        Page<Post> emptyPostPage = Page.empty();
        PageableResponse<PostResponse> expectedResponse = new PageableResponse<>(0L, 0, 0, 10, new ArrayList<>());

        when(postRepository.findByAuthor(author, PageRequest.of(pageNumber, size))).thenReturn(emptyPostPage);
        when(imageClient.getImagesByParentId(anyList(), eq(pageNumber), eq(size))).thenReturn(new PageableResponse<>(0L, 0, 0, 10, new ArrayList<>()));
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getPostsByAuthor(pageNumber, size, author);

        assertNotNull(result);
        assertEquals(0L, result.totalElements());
    }

    @Test
    void delete_ShouldHandleMultipleCalls() {
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();
        Post post1 = createTestPost();
        Post post2 = createTestPost();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId1), any(PostNotFoundException.class)))
                .thenReturn(post1);
        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId2), any(PostNotFoundException.class)))
                .thenReturn(post2);
        doNothing().when(postRepository).deleteById(any(UUID.class));
        doNothing().when(imageClient).deleteImageByParentId(any(UUID.class));

        postService.delete(postId1);
        postService.delete(postId2);

        verify(postRepository, times(2)).deleteById(any(UUID.class));
        verify(imageClient, times(2)).deleteImageByParentId(any(UUID.class));
    }

    @Test
    void getById_ShouldHandleMultipleCalls() {
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();
        Post post1 = createTestPost();
        Post post2 = createTestPost();
        ImageResponse imageResponse = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file".getBytes(), "name")), postId1);

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId1), any(PostNotFoundException.class)))
                .thenReturn(post1);
        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId2), any(PostNotFoundException.class)))
                .thenReturn(post2);
        when(imageClient.getImageByParentId(any(UUID.class))).thenReturn(imageResponse);
        when(postMapper.toResponse(any(Post.class), anyList())).thenReturn(createTestPostResponse());

        postService.getById(postId1);
        postService.getById(postId2);

        verify(imageClient, times(2)).getImageByParentId(any(UUID.class));
        verify(postMapper, times(2)).toResponse(any(Post.class), anyList());
    }

    private Post createTestPost() {
        return Post.builder()
                .id(UUID.randomUUID())
                .text("Test text")
                .author("Test author")
                .authorId(UUID.randomUUID())
                .numberOfLikes(0)
                .numberOfComments(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PostResponse createTestPostResponse() {
        return new PostResponse(
                UUID.randomUUID(),
                "Test text",
                "Test author",
                UUID.randomUUID(),
                0,
                0,
                LocalDateTime.now(),
                new ArrayList<>()
        );
    }

    private Page<Post> createTestPostPage() {
        List<Post> posts = List.of(createTestPost(), createTestPost());
        return new PageImpl<>(posts);
    }

    private PageableResponse<ImageResponse> createTestImageResponse() {
        ImageResponse imageResponse = new ImageResponse(
                ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file".getBytes(), "name")),
                UUID.randomUUID()
        );
        return new PageableResponse<>(2L, 1, 0, 10, List.of(imageResponse));
    }

    private PageableResponse<PostResponse> createTestPageableResponse() {
        return new PageableResponse<>(2L, 1, 0, 10, List.of(createTestPostResponse(), createTestPostResponse()));
    }

    @Test
    void getById_ShouldReturnPostResponse_WhenPostExists() {
        UUID postId = UUID.randomUUID();
        Post post = createTestPost();
        ImageResponse imageResponse = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file".getBytes(), "name")), postId);
        PostResponse expectedResponse = createTestPostResponse();

        when(postValidationService.findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class)))
                .thenReturn(post);
        when(imageClient.getImageByParentId(postId)).thenReturn(imageResponse);
        when(postMapper.toResponse(eq(post), anyList())).thenReturn(expectedResponse);

        PostResponse result = postService.getById(postId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(postValidationService).findByIdThrowsExceptionIfDoesntExist(eq(postId), any(PostNotFoundException.class));
        verify(imageClient).getImageByParentId(postId);
        verify(postMapper).toResponse(eq(post), anyList());
    }

    @Test
    void getFeed_ShouldReturnPageableResponse() {
        int pageNumber = 0;
        int size = 10;
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();

        Post post1 = createTestPost();
        post1.setId(postId1);
        Post post2 = createTestPost();
        post2.setId(postId2);

        Page<Post> postPage = new PageImpl<>(List.of(post1, post2));

        ImageResponse imageResponse1 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file1".getBytes(), "name1")), postId1);
        ImageResponse imageResponse2 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file2".getBytes(), "name2")), postId2);

        PageableResponse<ImageResponse> imagesResponse = new PageableResponse<>(2L, 1, 0, 10,
                List.of(imageResponse1, imageResponse2));

        PageableResponse<PostResponse> expectedResponse = createTestPageableResponse();

        when(postRepository.findAll(PageRequest.of(pageNumber, size))).thenReturn(postPage);
        when(imageClient.getImagesByParentId(List.of(postId1, postId2), pageNumber, size)).thenReturn(imagesResponse);
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getFeed(pageNumber, size);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(postRepository).findAll(PageRequest.of(pageNumber, size));
        verify(imageClient).getImagesByParentId(List.of(postId1, postId2), pageNumber, size);
        verify(pageMapper).toResponse(any(Page.class));
    }

    @Test
    void getFeed_ShouldHandleDifferentPageSizes() {
        int pageNumber = 1;
        int size = 5;
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();

        Post post1 = createTestPost();
        post1.setId(postId1);
        Post post2 = createTestPost();
        post2.setId(postId2);

        Page<Post> postPage = new PageImpl<>(List.of(post1, post2));

        ImageResponse imageResponse1 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file1".getBytes(), "name1")), postId1);
        ImageResponse imageResponse2 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file2".getBytes(), "name2")), postId2);

        PageableResponse<ImageResponse> imagesResponse = new PageableResponse<>(2L, 1, 0, 5,
                List.of(imageResponse1, imageResponse2));

        PageableResponse<PostResponse> expectedResponse = createTestPageableResponse();

        when(postRepository.findAll(PageRequest.of(pageNumber, size))).thenReturn(postPage);
        when(imageClient.getImagesByParentId(anyList(), eq(pageNumber), eq(size))).thenReturn(imagesResponse);
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getFeed(pageNumber, size);

        assertNotNull(result);
        verify(postRepository).findAll(PageRequest.of(pageNumber, size));
    }

    @Test
    void create_ShouldHandleNullAuthorId() throws IOException {
        PostRequest request = new PostRequest("text", "author", null);
        List<MultipartFile> files = List.of(multipartFile);
        Post post = createTestPost();
        byte[] fileBytes = "file".getBytes();

        when(postMapper.toEntity(request)).thenReturn(post);
        when(postRepository.saveAndFlush(post)).thenReturn(post);
        when(multipartFile.getBytes()).thenReturn(fileBytes);
        when(postMapper.toResponse(eq(post), anyList())).thenReturn(createTestPostResponse());

        PostResponse result = postService.create(request, files);

        assertNotNull(result);
        verify(postMapper).toEntity(request);
    }

    @Test
    void getPostsByAuthor_ShouldReturnPageableResponse() {
        int pageNumber = 0;
        int size = 10;
        String author = "testAuthor";
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();

        Post post1 = createTestPost();
        post1.setId(postId1);
        post1.setAuthor(author);
        Post post2 = createTestPost();
        post2.setId(postId2);
        post2.setAuthor(author);

        Page<Post> postPage = new PageImpl<>(List.of(post1, post2));

        ImageResponse imageResponse1 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file1".getBytes(), "name1")), postId1);
        ImageResponse imageResponse2 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file2".getBytes(), "name2")), postId2);

        PageableResponse<ImageResponse> imagesResponse = new PageableResponse<>(2L, 1, 0, 10,
                List.of(imageResponse1, imageResponse2));

        PageableResponse<PostResponse> expectedResponse = createTestPageableResponse();

        when(postRepository.findByAuthor(author, PageRequest.of(pageNumber, size))).thenReturn(postPage);
        when(imageClient.getImagesByParentId(List.of(postId1, postId2), pageNumber, size)).thenReturn(imagesResponse);
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getPostsByAuthor(pageNumber, size, author);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(postRepository).findByAuthor(author, PageRequest.of(pageNumber, size));
        verify(imageClient).getImagesByParentId(List.of(postId1, postId2), pageNumber, size);
        verify(pageMapper).toResponse(any(Page.class));
    }

    @Test
    void getPostsByAuthor_ShouldHandleSpecialCharactersInAuthorName() {
        int pageNumber = 0;
        int size = 10;
        String author = "test@author#123";
        UUID postId1 = UUID.randomUUID();

        Post post1 = createTestPost();
        post1.setId(postId1);
        post1.setAuthor(author);

        Page<Post> postPage = new PageImpl<>(List.of(post1));

        ImageResponse imageResponse1 = new ImageResponse(ImageType.POST_ATTACHMENT,
                List.of(new FileTransferEntity("file1".getBytes(), "name1")), postId1);

        PageableResponse<ImageResponse> imagesResponse = new PageableResponse<>(1L, 1, 0, 10,
                List.of(imageResponse1));

        PageableResponse<PostResponse> expectedResponse = new PageableResponse<>(1L, 1, 0, 10,
                List.of(createTestPostResponse()));

        when(postRepository.findByAuthor(author, PageRequest.of(pageNumber, size))).thenReturn(postPage);
        when(imageClient.getImagesByParentId(List.of(postId1), pageNumber, size)).thenReturn(imagesResponse);
        when(pageMapper.toResponse(any(Page.class))).thenReturn(expectedResponse);

        PageableResponse<PostResponse> result = postService.getPostsByAuthor(pageNumber, size, author);

        assertNotNull(result);
        verify(postRepository).findByAuthor(author, PageRequest.of(pageNumber, size));
    }
}