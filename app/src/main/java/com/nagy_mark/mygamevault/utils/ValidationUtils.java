package com.nagy_mark.mygamevault.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return Pattern.matches(EMAIL_REGEX, email);
    }

    public static boolean isPasswordStrong(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        if (password.length() < 8) return false;

        if (!password.matches(".*[A-Z].*")) return false;

        if (!password.matches(".*[a-z].*")) return false;

        if (!password.matches(".*[0-9].*")) return false;

        if (!password.matches(".*[^a-zA-Z0-9].*")) return false;

        return true;
    }

    public static boolean isPasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }

        return password.equals(confirmPassword);
    }
}
