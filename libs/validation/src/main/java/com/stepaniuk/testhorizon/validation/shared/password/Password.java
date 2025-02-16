package com.stepaniuk.testhorizon.validation.shared.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordValidator.class)
public @interface Password {
    String message() default "{validation.constraints.shared.password}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
