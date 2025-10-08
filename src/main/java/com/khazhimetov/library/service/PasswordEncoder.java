package com.khazhimetov.library.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Password encoder utility using BCrypt
 */
public class PasswordEncoder {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Encode password
     * @param rawPassword raw password
     * @return encoded password
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verify password
     * @param rawPassword raw password
     * @param encodedPassword encoded password
     * @return true if passwords match
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
