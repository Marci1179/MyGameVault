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
}
