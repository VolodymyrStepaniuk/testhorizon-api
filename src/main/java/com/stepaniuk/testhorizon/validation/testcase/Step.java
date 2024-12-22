package com.stepaniuk.testhorizon.validation.testcase;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.*;

@Target({ElementType.TYPE_USE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 255)
public @interface Step {
    String message() default "{validation.constraints.testcase.step}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
