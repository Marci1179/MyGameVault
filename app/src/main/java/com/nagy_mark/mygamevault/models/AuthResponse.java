package com.nagy_mark.mygamevault.models;

public class AuthResponse {
    private String access_token;
    private String refresh_token;
    private User user;

    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        private String id;

        public String getId() {
            return id;
        }
    }
}
