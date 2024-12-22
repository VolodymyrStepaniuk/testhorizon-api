package com.stepaniuk.testhorizon.validation.shared;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;

import java.lang.annotation.*;

@Target({ElementType.TYPE_USE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Max(Integer.MAX_VALUE)
public @interface Id {
    String message() default "{validation.constraints.shared.id}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
