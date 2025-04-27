package com.stepaniuk.testhorizon.validation.bugreport;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Length(min = 1, max = 512)
public @interface Environment {
    String message() default "{validation.constraints.bugreport.environment}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
