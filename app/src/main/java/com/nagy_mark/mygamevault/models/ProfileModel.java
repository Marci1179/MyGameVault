package com.nagy_mark.mygamevault.models;

public class ProfileModel {
    private String id;
    private String username;
    private String avatar_url;

    public ProfileModel(String id, String username, String avatar_url) {
        this.id = id;
        this.username = username;
        this.avatar_url = avatar_url;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public void setAvatarUrl(String avatar_url) {
        this.avatar_url = avatar_url;
    }
}
