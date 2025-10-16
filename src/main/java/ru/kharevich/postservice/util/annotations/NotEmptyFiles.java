package ru.kharevich.postservice.util.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.kharevich.postservice.util.validation.NotEmptyFilesValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyFilesValidator.class)
@Documented
public @interface NotEmptyFiles {
    String message() default "Must contain at least one valid file";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}