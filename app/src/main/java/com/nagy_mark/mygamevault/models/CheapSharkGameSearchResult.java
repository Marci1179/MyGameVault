package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class CheapSharkGameSearchResult {
    @SerializedName("gameID")
    private String gameId;

    @SerializedName("external")
    private String externalName;

    public String getGameId() {
        return gameId;
    }

    public String getExternalName() {
        return externalName;
    }
}