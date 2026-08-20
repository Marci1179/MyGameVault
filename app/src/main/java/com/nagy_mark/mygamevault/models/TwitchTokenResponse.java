package com.nagy_mark.mygamevault.models;

public class TwitchTokenResponse {
    private String access_token;
    private int expires_in;

    public String getAccessToken() {
        return access_token;
    }

    public int getExpiresIn() {
        return expires_in;
    }
}
