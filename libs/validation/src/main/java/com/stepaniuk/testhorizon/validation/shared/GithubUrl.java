package com.stepaniuk.testhorizon.validation.shared;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 2048)
@URL
public @interface GithubUrl {
    String message() default "{validation.constraints.shared.githubUrl}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
