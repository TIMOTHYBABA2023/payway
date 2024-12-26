package com.payway.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BVNValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBVN {
    String message() default "Invalid BVN format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}