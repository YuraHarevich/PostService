package ru.kharevich.postservice.util.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.postservice.util.annotations.NotEmptyFiles;

import java.util.List;
import java.util.Objects;

public class NotEmptyFilesValidator implements ConstraintValidator<NotEmptyFiles, List<MultipartFile>> {

    @Override
    public void initialize(NotEmptyFiles constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        return files.stream().anyMatch(this::isValidFile);
    }

    private boolean isValidFile(MultipartFile file) {
        return file != null && !file.isEmpty() && !Objects.equals(file.getOriginalFilename(), "");
    }
}
