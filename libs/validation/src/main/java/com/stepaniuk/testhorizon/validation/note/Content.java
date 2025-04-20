package com.stepaniuk.testhorizon.validation.note;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 1024)
public @interface Content {
    String message() default "{validation.constraints.note.content}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
