package com.payway.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BVNValidator implements ConstraintValidator<ValidBVN, String> {
    @Override
    public void initialize(ValidBVN constraintAnnotation) {
    }

    @Override
    public boolean isValid(String bvn, ConstraintValidatorContext context) {
        if (bvn == null) {
            return false;
        }
        // BVN must be exactly 11 digits
        return bvn.matches("^\\d{11}$");
    }
}