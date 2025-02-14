package com.stepaniuk.testhorizon.validation.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 255)
public @interface FirstName {
    String message() default "{validation.constraints.user.firstName}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
