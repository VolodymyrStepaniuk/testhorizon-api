package com.stepaniuk.testhorizon.validation.shared;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

@Target({ElementType.TYPE_USE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Max(Integer.MAX_VALUE)
@Min(Integer.MIN_VALUE)
public @interface Rating {
    String message() default "{validation.constraints.shared.rating}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}