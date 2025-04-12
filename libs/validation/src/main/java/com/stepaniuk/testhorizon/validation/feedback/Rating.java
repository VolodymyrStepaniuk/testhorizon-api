package com.stepaniuk.testhorizon.validation.feedback;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Max(5)
@Min(1)
public @interface Rating {
    String message() default "{validation.constraints.feedback.rating}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
