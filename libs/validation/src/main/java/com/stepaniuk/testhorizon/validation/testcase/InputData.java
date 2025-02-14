package com.stepaniuk.testhorizon.validation.testcase;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 512)
public @interface InputData {
    String message() default "{validation.constraints.testcase.inputData}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
