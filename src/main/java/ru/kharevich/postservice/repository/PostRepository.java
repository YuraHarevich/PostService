package ru.kharevich.postservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kharevich.postservice.dto.response.PostResponse;
import ru.kharevich.postservice.model.Post;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByAuthor(String author, Pageable pageable);
}
