package com.nagy_mark.mygamevault.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MyGameVaultPrefs";
    private static final String KEY_JWT_TOKEN = "JWT_TOKEN";
    private static final String KEY_REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_TWITCH_TOKEN = "TWITCH_TOKEN";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String jwtToken, String refreshToken, String userId) {
        editor.putString(KEY_JWT_TOKEN, jwtToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public void clearSession() {
        editor.remove(KEY_JWT_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.apply();
    }

    public boolean isLoggedIn() {
        String token = getJwtToken();
        return token != null && !token.isEmpty();
    }

    public String getJwtToken() {
        return prefs.getString(KEY_JWT_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void saveTwitchToken(String token) {
        editor.putString(KEY_TWITCH_TOKEN, token);
        editor.apply();
    }

    public String getTwitchToken() {
        return prefs.getString(KEY_TWITCH_TOKEN, null);
    }
}
