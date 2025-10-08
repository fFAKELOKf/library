package com.khazhimetov.library.util;

import com.khazhimetov.library.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Validation utility class
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Validate email format
     * @param email email to validate
     * @throws ValidationException if email is invalid
     */
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "Invalid email format");
        }
    }

    /**
     * Validate password
     * @param password password to validate
     * @throws ValidationException if password is invalid
     */
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("password", "Password is required");
        }
        if (password.length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters");
        }
    }

    /**
     * Validate required field
     * @param value field value
     * @param fieldName field name
     * @throws ValidationException if field is empty
     */
    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
    }

    /**
     * Validate positive number
     * @param value number value
     * @param fieldName field name
     * @throws ValidationException if number is not positive
     */
    public static void validatePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be positive");
        }
    }
}
