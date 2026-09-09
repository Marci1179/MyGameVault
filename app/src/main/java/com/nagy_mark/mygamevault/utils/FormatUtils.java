package com.nagy_mark.mygamevault.utils;

import java.util.Calendar;

public class FormatUtils {
    public static String extractYear(String rawDate, String fallback) {
        if (rawDate != null && rawDate.length() >= 4) {
            return rawDate.substring(0, 4);
        }
        return fallback;
    }

    public static String extractYear(Long timestamp, String fallback) {
        if (timestamp != null && timestamp > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp * 1000);
            return String.valueOf(calendar.get(Calendar.YEAR));
        }
        return fallback;
    }
}
