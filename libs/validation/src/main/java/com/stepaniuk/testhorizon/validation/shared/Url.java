package com.stepaniuk.testhorizon.validation.shared;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.lang.annotation.*;

@Target({ElementType.TYPE_USE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 2048)
@URL
public @interface Url {
    String message() default "{validation.constraints.shared.url}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
