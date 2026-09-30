package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class GameDataModel {
    @SerializedName("id")
    private Long id;

    @SerializedName("igdb_id")
    private long igdbId;

    @SerializedName("game_name")
    private String gameName;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("publisher")
    private String publisher;

    @SerializedName("cover")
    private String cover;

    public GameDataModel(long igdbId, String gameName, String releaseDate, String publisher, String cover) {
        this.igdbId = igdbId;
        this.gameName = gameName;
        this.releaseDate = releaseDate;
        this.publisher = publisher;
        this.cover = cover;
    }

    public Long getId() {
        return id;
    }
}
