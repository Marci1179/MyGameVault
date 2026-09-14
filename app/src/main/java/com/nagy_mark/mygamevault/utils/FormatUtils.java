package com.nagy_mark.mygamevault.utils;

import android.icu.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    public static String formatIgdbDate(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return null;
        }
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
}
