package com.stepaniuk.testhorizon.validation.feedback;

import jakarta.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 255)
public @interface Comment {
    String message() default "{validation.constraints.feedback.comment}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
