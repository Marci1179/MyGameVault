package com.nagy_mark.mygamevault.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidationUtilsTest {
    @Test
    public void testIsValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("test@test.hu"));
        assertTrue(ValidationUtils.isValidEmail("test.elek123@gmail.com"));
        assertTrue(ValidationUtils.isValidEmail("elek+tag@domain.gov.hu"));

        assertFalse(ValidationUtils.isValidEmail("test@.com"));
        assertFalse(ValidationUtils.isValidEmail("test@com"));
        assertFalse(ValidationUtils.isValidEmail("test.com"));
        assertFalse(ValidationUtils.isValidEmail(" "));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    public void testIsPasswordStrong() {
        assertTrue(ValidationUtils.isPasswordStrong("Strong@123"));
        assertTrue(ValidationUtils.isPasswordStrong("aB3defgh!"));
        assertTrue(ValidationUtils.isPasswordStrong("StrongP@ss2026"));

        assertFalse(ValidationUtils.isPasswordStrong("Str@1"));
        assertFalse(ValidationUtils.isPasswordStrong("Jelszo123"));
        assertFalse(ValidationUtils.isPasswordStrong("jelszo@123"));
        assertFalse(ValidationUtils.isPasswordStrong("JELSZO@123"));
        assertFalse(ValidationUtils.isPasswordStrong("Jelszo@Pass"));
        assertFalse(ValidationUtils.isPasswordStrong(""));
        assertFalse(ValidationUtils.isPasswordStrong(null));
    }

    @Test
    public void testIsPasswordMatch() {
        assertTrue(ValidationUtils.isPasswordMatch("Strong@123", "Strong@123"));

        assertFalse(ValidationUtils.isPasswordMatch("Strong@123", "Strong@12"));
        assertFalse(ValidationUtils.isPasswordMatch("StrongP@ss2026", "Strongp@ss2026"));
        assertFalse(ValidationUtils.isPasswordMatch(null, "Strong@123"));
    }

    @Test
    public void testIsValidOtp() {
        assertTrue(ValidationUtils.isValidOtp("12345678"));
        assertTrue(ValidationUtils.isValidOtp("15467428"));

        assertFalse(ValidationUtils.isValidOtp("1234567"));
        assertFalse(ValidationUtils.isValidOtp("123456789"));
        assertFalse(ValidationUtils.isValidOtp("123a5678"));
        assertFalse(ValidationUtils.isValidOtp("asdfghjk"));
        assertFalse(ValidationUtils.isValidOtp("123-4567"));
        assertFalse(ValidationUtils.isValidOtp("1234567 "));
        assertFalse(ValidationUtils.isValidOtp(" 1234567"));
        assertFalse(ValidationUtils.isValidOtp(""));
        assertFalse(ValidationUtils.isValidOtp(null));
    }
}
