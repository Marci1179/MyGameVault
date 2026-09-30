package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class UserGameModel {
    @SerializedName("game_id")
    private long gameId;

    @SerializedName("status_id")
    private int statusId;

    @SerializedName("user_id")
    private String userId;

    public UserGameModel(long gameId, int statusId, String userId) {
        this.gameId = gameId;
        this.statusId = statusId;
        this.userId = userId;
    }
}
