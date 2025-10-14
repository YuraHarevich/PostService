package ru.kharevich.postservice.util.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kharevich.postservice.model.Post;
import ru.kharevich.postservice.repository.PostRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostValidationService {

    private final PostRepository postRepository;

    public Post findByIdThrowsExceptionIfDoesntExist(UUID id, RuntimeException exception) {
        return postRepository.findById(id)
                .orElseThrow(() -> exception);
    }

}
