package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SavedGameModel implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("game_id")
    private long gameId;

    @SerializedName("status_id")
    private int statusId;

    @SerializedName("rating")
    private Float rating;

    @SerializedName("note")
    private String note;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    @SerializedName("game_data")
    private GameDataNested gameData;

    public static class GameDataNested implements Serializable {
        @SerializedName("game_name")
        public String gameName;

        @SerializedName("release_date")
        public String releaseDate;

        @SerializedName("publisher")
        public String publisher;

        @SerializedName("cover")
        public String cover;
    }

    public long getId() {
        return id;
    }

    public long getGameId() {
        return gameId;
    }

    public int getStatusId() {
        return statusId;
    }

    public Float getRating() {
        return rating;
    }

    public String getNote() {
        return note;
    }

    public String getUserId() {
        return userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public GameDataNested getGameData() {
        return gameData;
    }

    public String getGameName() {
        return (gameData != null) ? gameData.gameName : null;
    }

    public String getReleaseDate() {
        return (gameData != null) ? gameData.releaseDate : null;
    }

    public String getPublisher() {
        return (gameData != null) ? gameData.publisher : null;
    }

    public String getCover() {
        return (gameData != null) ? gameData.cover : null;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public void setGameData(GameDataNested gameData) {
        this.gameData = gameData;
    }
}