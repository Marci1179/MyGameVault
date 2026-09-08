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
}
