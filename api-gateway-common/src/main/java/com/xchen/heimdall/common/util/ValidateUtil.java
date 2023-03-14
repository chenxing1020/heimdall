package com.xchen.heimdall.common.util;

import org.springframework.lang.NonNull;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author xchen
 */
public class ValidateUtil {

    private ValidateUtil() {
    }

    // 它是线程安全的
    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    public static <T> void validate(@NonNull T t) {
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(t);
        if (!constraintViolations.isEmpty()) {
            StringBuilder validateError = new StringBuilder();
            for (ConstraintViolation<T> constraintViolation : constraintViolations) {
                validateError.append(constraintViolation.getPropertyPath())
                        .append(" ")
                        .append(constraintViolation.getMessage())
                        .append("\n");
            }
            throw new IllegalArgumentException(validateError.toString());
        }
    }

}
