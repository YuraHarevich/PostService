package ru.kharevich.postservice.util.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.kharevich.postservice.dto.request.PostRequest;
import ru.kharevich.postservice.dto.response.PostResponse;
import ru.kharevich.postservice.model.Post;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PostMapper {
    PostResponse toResponse(Post post, byte[] file);

    Post toEntity(PostRequest postRequest);

    void updateEntityByRequest(PostRequest driverRequest, @MappingTarget Post post);
}
