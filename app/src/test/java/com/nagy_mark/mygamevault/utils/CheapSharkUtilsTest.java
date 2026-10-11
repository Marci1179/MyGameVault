package com.nagy_mark.mygamevault.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CheapSharkUtilsTest {
    @Test
    public void testGetStoreName_ValidIds() {
        assertEquals("Steam", CheapSharkUtils.getStoreName("1"));
        assertEquals("Ubisoft", CheapSharkUtils.getStoreName("13"));
        assertEquals("Epic Games", CheapSharkUtils.getStoreName("25"));
    }

    @Test
    public void testGetStoreName_InvalidId_ReturnsNull() {
        assertNull(CheapSharkUtils.getStoreName("999"));
        assertNull(CheapSharkUtils.getStoreName(""));
        assertNull(CheapSharkUtils.getStoreName("szoveg"));
    }

    @Test
    public void testGetStoreName_NullInput_ReturnsNull() {
        assertNull(CheapSharkUtils.getStoreName(null));
    }

    @Test
    public void testToggleRomanNumerals_RomanToArabic() {
        assertEquals("Final Fantasy 7", CheapSharkUtils.toggleRomanNumerals("Final Fantasy VII"));
        assertEquals("Resident Evil 8", CheapSharkUtils.toggleRomanNumerals("Resident Evil VIII"));
        assertEquals("Street Fighter 6", CheapSharkUtils.toggleRomanNumerals("Street Fighter VI"));
    }

    @Test
    public void testToggleRomanNumerals_ArabicToRoman() {
        assertEquals("The Witcher III", CheapSharkUtils.toggleRomanNumerals("The Witcher 3"));
        assertEquals("Diablo IV", CheapSharkUtils.toggleRomanNumerals("Diablo 4"));
        assertEquals("F1 XX", CheapSharkUtils.toggleRomanNumerals("F1 20"));
    }

    @Test
    public void testToggleRomanNumerals_ReturnsNull() {
        assertNull(CheapSharkUtils.toggleRomanNumerals("Civilization"));
        assertNull(CheapSharkUtils.toggleRomanNumerals("Cyberpunk 2077"));
        assertNull(CheapSharkUtils.toggleRomanNumerals("Hollow Knight"));
        assertNull(CheapSharkUtils.toggleRomanNumerals(null));
        assertNull(CheapSharkUtils.toggleRomanNumerals(""));
    }
}
