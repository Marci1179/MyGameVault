package com.nagy_mark.mygamevault.models;

import java.io.Serializable;

public class SavedGameModel implements Serializable {
    private int id;
    private String game_name;
    private String release_year;
    private String publisher;
    private String cover;
    private int status_id;
    private Float rating;
    private String note;
    private String user_id;
    private String created_at;

    public int getId() {
        return id;
    }

    public String getGameName() {
        return game_name;
    }

    public String getReleaseYear() {
        return release_year;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getCover() {
        return cover;
    }

    public int getStatusId() {
        return status_id;
    }

    public Float getRating() {
        return rating;
    }

    public String getNote() {
        return note;
    }

    public String getUserId() {
        return user_id;
    }

    public String getCreatedAt() {
        return created_at;
    }
}