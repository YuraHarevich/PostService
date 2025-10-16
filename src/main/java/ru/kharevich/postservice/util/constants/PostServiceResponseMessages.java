package ru.kharevich.postservice.util.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostServiceResponseMessages {

    public static final String IMAGE_NOT_FOUND_MESSAGE = "image not found";

    public static final String ERROR_IN_IMAGE_SERVICE_MESSAGE = "error while trying to communicate with image service";

    public static final String IMAGE_PARSE_ERROR_MESSAGE = "unable to parse image file";

    public static final String POST_NOT_FOUND_MESSAGE = "post not found";

    public static final String NO_FILES_UPLOADED_MESSAGE = "Must contain at least one valid file";

}
