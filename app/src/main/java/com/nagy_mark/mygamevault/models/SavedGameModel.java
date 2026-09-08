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
    private boolean is_favorite;

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

    public boolean isFavorite() {
        return is_favorite;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGameName(String gameName) {
        this.game_name = gameName;
    }

    public void setReleaseYear(String releaseYear) {
        this.release_year = releaseYear;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setStatusId(int statusId) {
        this.status_id = statusId;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public void setCreatedAt(String createdAt) {
        this.created_at = createdAt;
    }

    public void setFavorite(boolean favorite) {
        this.is_favorite = favorite;
    }
}