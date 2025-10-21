package ru.kharevich.postservice.dto.response;

import java.util.UUID;

/**
 * @param postId Post ID
 * @param numberOfLikes Total number of likes for the post
 * @param numberOfComments Total number of comments for the post
 */
public record ActivityResponse(
        UUID postId,

        Integer numberOfLikes,

        Integer numberOfComments
) {}
