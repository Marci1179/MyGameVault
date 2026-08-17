package com.nagy_mark.mygamevault.models;

public class AuthResponse {
    private String access_token;
    private String refresh_token;

    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }
}
