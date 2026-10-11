package com.nagy_mark.mygamevault.utils;

public class CheapSharkUtils {
    public static String getStoreName(String storeId) {
        if (storeId == null) {
            return null;
        }

        switch (storeId) {
            case "1": return "Steam";
            case "3": return "GreenManGaming";
            case "7": return "GOG";
            case "8": return "EA/Origin";
            case "11": return "Humble Store";
            case "13": return "Ubisoft";
            case "15": return "Fanatical";
            case "25": return "Epic Games";
            default: return null;
        }
    }

    public static String toggleRomanNumerals(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }

        String toArabic = title
                .replaceAll("(?i)\\bXX\\b", "20")
                .replaceAll("(?i)\\bXIX\\b", "19")
                .replaceAll("(?i)\\bXVIII\\b", "18")
                .replaceAll("(?i)\\bXVII\\b", "17")
                .replaceAll("(?i)\\bXVI\\b", "16")
                .replaceAll("(?i)\\bXV\\b", "15")
                .replaceAll("(?i)\\bXIV\\b", "14")
                .replaceAll("(?i)\\bXIII\\b", "13")
                .replaceAll("(?i)\\bXII\\b", "12")
                .replaceAll("(?i)\\bXI\\b", "11")
                .replaceAll("(?i)\\bX\\b", "10")
                .replaceAll("(?i)\\bIX\\b", "9")
                .replaceAll("(?i)\\bVIII\\b", "8")
                .replaceAll("(?i)\\bVII\\b", "7")
                .replaceAll("(?i)\\bVI\\b", "6")
                .replaceAll("(?i)\\bV\\b", "5")
                .replaceAll("(?i)\\bIV\\b", "4")
                .replaceAll("(?i)\\bIII\\b", "3")
                .replaceAll("(?i)\\bII\\b", "2");

        if (!toArabic.equals(title)) {
            return toArabic;
        }

        String toRoman = title
                .replaceAll("\\b20\\b", "XX")
                .replaceAll("\\b19\\b", "XIX")
                .replaceAll("\\b18\\b", "XVIII")
                .replaceAll("\\b17\\b", "XVII")
                .replaceAll("\\b16\\b", "XVI")
                .replaceAll("\\b15\\b", "XV")
                .replaceAll("\\b14\\b", "XIV")
                .replaceAll("\\b13\\b", "XIII")
                .replaceAll("\\b12\\b", "XII")
                .replaceAll("\\b11\\b", "XI")
                .replaceAll("\\b10\\b", "X")
                .replaceAll("\\b9\\b", "IX")
                .replaceAll("\\b8\\b", "VIII")
                .replaceAll("\\b7\\b", "VII")
                .replaceAll("\\b6\\b", "VI")
                .replaceAll("\\b5\\b", "V")
                .replaceAll("\\b4\\b", "IV")
                .replaceAll("\\b3\\b", "III")
                .replaceAll("\\b2\\b", "II");

        if (!toRoman.equals(title)) {
            return toRoman;
        }

        return null;
    }
}
