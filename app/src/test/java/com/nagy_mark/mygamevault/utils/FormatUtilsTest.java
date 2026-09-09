package com.nagy_mark.mygamevault.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FormatUtilsTest {
    @Test
    public void testExtractYearFromString_ValidFullDate() {
        assertEquals("2025", FormatUtils.extractYear("2025-08-11", "Ismeretlen év"));
    }

    @Test
    public void testExtractYearFromString_OnlyYearProvided() {
        assertEquals("2026", FormatUtils.extractYear("2026", "Ismeretlen év"));
    }

    @Test
    public void testExtractYearFromString_ShortString() {
        assertEquals("Ismeretlen év", FormatUtils.extractYear("99", "Ismeretlen év"));
    }

    @Test
    public void testExtractYearFromString_EmptyString() {
        assertEquals("N/A", FormatUtils.extractYear("", "N/A"));
    }

    @Test
    public void testExtractYearFromString_NullInput() {
        assertEquals("Ismeretlen év", FormatUtils.extractYear((String) null, "Ismeretlen év"));
    }

    @Test
    public void testExtractYearFromTimestamp_ValidTimestamp() {
        assertEquals("2020", FormatUtils.extractYear(1607558400L, "Ismeretlen év"));
    }

    @Test
    public void testExtractYearFromTimestamp_NullInput() {
        assertEquals("N/A", FormatUtils.extractYear((Long) null, "N/A"));
    }

    @Test
    public void testExtractYearFromTimestamp_Zero() {
        assertEquals("Ismeretlen", FormatUtils.extractYear(0L, "Ismeretlen"));
    }

    @Test
    public void testExtractYearFromTimestamp_Negative() {
        assertEquals("Ismeretlen", FormatUtils.extractYear(-100L, "Ismeretlen"));
    }
}
